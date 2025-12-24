package com.example.aotealApp.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.aotealApp.document.AppDocument;
import com.example.aotealApp.dto.AppDTO;
import com.example.aotealApp.dto.AppDetailDTO;
import com.example.aotealApp.entity.App;
import com.example.aotealApp.entity.AppVersion;
import com.example.aotealApp.entity.VersionStatus;
import com.example.aotealApp.repository.AppRepository;
import com.example.aotealApp.repository.AppSearchRepository;
import com.example.aotealApp.repository.AppVersionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppService {
    private final AppRepository appRepository;
    private final AppVersionRepository appVersionRepository;
    private final MinioStorageService storageService;
    private final VirusScanService virusScanService;
    private final AppSearchRepository appSearchRepository;

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
        // ✅ CHỈ LẤY CÁC VERSION ĐÃ PUBLISHED
        return appVersionRepository.findByStatus(VersionStatus.PUBLISHED).stream()
                .collect(Collectors.groupingBy(v -> v.getApp().getId()))
                .values().stream()
                .map(versions -> {
                    // Lấy version mới nhất
                    AppVersion latest = versions.stream()
                            .max((v1, v2) -> v1.getCreatedAt().compareTo(v2.getCreatedAt()))
                            .orElse(null);

                    if (latest == null)
                        return null;

                    AppDTO dto = new AppDTO();
                    dto.setId(latest.getApp().getId());
                    dto.setName(latest.getApp().getName());
                    dto.setDescription(latest.getApp().getDescription());
                    dto.setLatestVersion(latest.getVersion());
                    dto.setUpdatedAt(latest.getCreatedAt());
                    dto.setDownloadUrl(storageService.getPresignedUrl(latest.getFileUrl()));

                    return dto;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    public List<AppDTO> getAppsByStatus(VersionStatus versionStatus) {

        return appVersionRepository.findAll().stream()
                .filter(v -> v.getStatus() == versionStatus)
                .map(version -> {
                    AppDTO dto = new AppDTO();
                    // Lưu ý: ID ở đây là ID của Version (để lát nữa Admin duyệt cái version này)
                    dto.setId(version.getId());

                    dto.setName(version.getApp().getName());
                    dto.setDescription(version.getApp().getDescription());
                    dto.setLatestVersion(version.getVersion());
                    dto.setUpdatedAt(version.getCreatedAt());

                    // Link tải preview cho Admin test thử trước khi duyệt
                    dto.setDownloadUrl(storageService.getPresignedUrl(version.getFileUrl()));

                    return dto;
                })
                .collect(Collectors.toList());

    }

    // --- 2. Hàm Duyệt hoặc Từ chối App ---
    @Transactional // Quan trọng để đảm bảo dữ liệu nhất quán
    public void approveApp(Long versionId, boolean isApproved) {
        // Tìm bản ghi version theo ID
        AppVersion version = appVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên bản ứng dụng với ID: " + versionId));

        if (isApproved) {
            // Nếu Admin bấm Duyệt
            version.setStatus(VersionStatus.PUBLISHED);
            version.setPublishedAt(java.time.LocalDateTime.now());
        } else {
            // Nếu Admin bấm Từ chối
            version.setStatus(VersionStatus.REJECTED);
            // Có thể thêm lý do từ chối vào releaseNote hoặc một field riêng nếu muốn
            version.setReleaseNote(version.getReleaseNote() + " [Admin đã từ chối]");
        }

        // Lưu xuống DB
        appVersionRepository.save(version);

        if (isApproved) {
            App app = version.getApp();
            AppDocument doc = new AppDocument();
            doc.setId(app.getId());
            doc.setName(app.getName());
            doc.setDescription(app.getDescription());
            doc.setPackageName(app.getPackageName());
            doc.setStatus("PUBLISHED");

            appSearchRepository.save(doc); // Lưu sang Elastic
        }
    }

    public AppDetailDTO getAppDetail(Long appId) {
        // 1. Tìm App gốc
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new RuntimeException("App not found"));

        // 2. Tìm tất cả version của app này
        // (Lưu ý: Chỉ lấy version đã PUBLISHED cho user thường xem)
        List<AppDTO> versionDtos = appVersionRepository.findAll().stream()
                .filter(v -> v.getApp().getId().equals(appId)) // Lọc theo App ID
                .filter(v -> v.getStatus() == VersionStatus.PUBLISHED) // Chỉ lấy bản đã duyệt
                .sorted((v1, v2) -> v2.getId().compareTo(v1.getId())) // Sắp xếp mới nhất lên đầu
                .map(v -> {
                    AppDTO dto = new AppDTO();
                    dto.setId(v.getId());
                    dto.setLatestVersion(v.getVersion()); // Tận dụng field này để chứa version
                    dto.setDescription(v.getReleaseNote()); // Tận dụng field desc để chứa Release Note
                    dto.setUpdatedAt(v.getCreatedAt());
                    dto.setDownloadUrl(storageService.getPresignedUrl(v.getFileUrl()));
                    return dto;
                })
                .collect(Collectors.toList());

        // 3. Map sang DTO trả về
        AppDetailDTO detail = new AppDetailDTO();
        detail.setId(app.getId());
        detail.setName(app.getName());
        detail.setDescription(app.getDescription());
        detail.setPackageName(app.getPackageName());
        detail.setVersions(versionDtos);

        return detail;
    }

}
