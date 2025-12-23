package com.example.aotealApp.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.aotealApp.dto.AppDTO;
import com.example.aotealApp.entity.App;
import com.example.aotealApp.entity.AppVersion;
import com.example.aotealApp.entity.VersionStatus;
import com.example.aotealApp.repository.AppRepository;
import com.example.aotealApp.repository.AppVersionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppService {
    private final AppRepository appRepository;
    private final AppVersionRepository appVersionRepository;
    private final MinioStorageService storageService;
    private final VirusScanService virusScanService;

    @Value("${minio.bucket}")
    private String bucketName;

    public AppVersion uploadNewAppVersion(String name, String packageName,
            String description, String version, String releaseNote,
            MultipartFile file) {

        App app = appRepository.findByPackageName(packageName)
                .orElseGet(() -> {
                    App newApp = new App();
                    newApp.setName(name);
                    newApp.setPackageName(packageName);
                    newApp.setDescription(description);
                    return appRepository.save(newApp);
                });

        // 2. Tao duong dan luu file trn Minio:
        // apps/{packageName}/{version}/{fileName}
        String fileName = file.getOriginalFilename();
        String storagePath = "apps/" + packageName + "/" + version
                + "/" + fileName;

        // 3. Upload len Minio
        String fileUrl = storageService.uploadFile(file, storagePath);

        // 4. Luu thong tin Version vao database
        AppVersion appVersion = new AppVersion();
        appVersion.setApp(app);
        appVersion.setVersion(version);
        appVersion.setReleaseNote(releaseNote);
        appVersion.setFileSize(file.getSize());
        appVersion.setFileUrl(fileUrl);
        appVersion.setStatus(VersionStatus.PENDING_SCAN); // mac dinh la nhap

        AppVersion savedVersion = appVersionRepository.save(appVersion);

        // --- GỌI HÀM QUÉT (ASYNC) ---
        // Hàm này sẽ chạy ngầm, code sẽ đi tiếp ngay lập tức
        virusScanService.scanFileAsync(savedVersion.getId(), bucketName);

        return savedVersion;

    }

    public List<AppDTO> getAllApps() {
        return appVersionRepository.findAll().stream()
                // Logic tạm: Lấy hết các version ra (sau này sẽ filter chỉ lấy bản mới nhất)
                .map(version -> {
                    AppDTO dto = new AppDTO();
                    dto.setId(version.getApp().getId());
                    dto.setName(version.getApp().getName());
                    dto.setDescription(version.getApp().getDescription());
                    dto.setLatestVersion(version.getVersion());
                    dto.setUpdatedAt(version.getCreatedAt());

                    // QUAN TRỌNG: Đổi path "apps/..." thành Link HTTP tải được
                    dto.setDownloadUrl(storageService.getPresignedUrl(version.getFileUrl()));

                    return dto;
                })
                .collect(Collectors.toList());
    }

}
