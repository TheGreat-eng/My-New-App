package com.example.aotealApp.services;

import java.io.InputStream;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.aotealApp.entity.AppVersion;
import com.example.aotealApp.entity.VersionStatus;
import com.example.aotealApp.repository.AppVersionRepository;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

@Service
@RequiredArgsConstructor
@Slf4j
public class VirusScanService {
    private final AppVersionRepository appVersionRepository;
    private final MinioClient minioClient;

    private final ClamavClient clamavClient = new ClamavClient("@Value(\"${clamav.host}\")", 3310);

    @Async
    public void scanFileAsync(Long appVersionId, String bucketName) {
        log.info("Bắt đầu quét virus cho AppVersion ID: {}", appVersionId);

        AppVersion appVersion = appVersionRepository.findById(appVersionId).orElse(null);

        if (appVersion == null)
            return;

        try {
            // 1. Lấy luồng dữ liệu file từ MinIO về (stream)
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(appVersion.getFileUrl())
                            .build());

            // 2. Gửi stream đó sang ClamAV để quét
            ScanResult result = clamavClient.scan(stream);

            // 3. Xử lý kết quả
            if (result instanceof ScanResult.OK) {
                log.info("File SẠCH. ID: {}", appVersionId);
                appVersion.setStatus(VersionStatus.PENDING_APPROVAL); // Hoặc PENDING_APPROVAL nếu có quy trình duyệt
            } else if (result instanceof ScanResult.VirusFound) {
                ScanResult.VirusFound virus = (ScanResult.VirusFound) result;
                log.warn("PHÁT HIỆN VIRUS!!! ID: {} - Tên virus: {}", appVersionId, virus.getFoundViruses());

                appVersion.setStatus(VersionStatus.REJECTED);
                appVersion.setReleaseNote(
                        appVersion.getReleaseNote() + " [BỊ TỪ CHỐI DO CÓ VIRUS: " + virus.getFoundViruses() + "]");

                // (Bài tập nâng cao: Bạn nên viết code xóa file khỏi MinIO luôn ở đây)
            }

        } catch (Exception e) {
            log.error("Lỗi khi quét virus: ", e);
            // Có thể set trạng thái ERROR để admin kiểm tra lại
        }

        // 4. Lưu trạng thái mới vào DB
        appVersionRepository.save(appVersion);

    }

}
