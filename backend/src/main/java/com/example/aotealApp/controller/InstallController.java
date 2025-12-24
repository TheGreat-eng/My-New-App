package com.example.aotealApp.controller;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aotealApp.entity.AppVersion;
import com.example.aotealApp.repository.AppVersionRepository;
import com.example.aotealApp.services.IosPlistService;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/install")
@RequiredArgsConstructor
public class InstallController {

    private final AppVersionRepository appVersionRepository;
    private final IosPlistService iosPlistService;
    private final MinioClient minioClient; // Inject trực tiếp MinioClient

    // Lấy link Cloudflare từ file cấu hình
    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${minio.bucket}")
    private String bucketName;

    // --- 1. API ĐIỀU HƯỚNG CÀI ĐẶT (QR CODE TRỎ VÀO ĐÂY) ---
    // @GetMapping("/{versionId}")
    // public ResponseEntity<?> installApp(@PathVariable Long versionId,
    // @RequestHeader(HttpHeaders.USER_AGENT) String userAgent) {
    // AppVersion version = appVersionRepository.findById(versionId)
    // .orElseThrow(() -> new RuntimeException("Version not found"));

    // // Tạo link tải file thông qua Proxy của Java (HTTPS xịn)
    // String proxyFileUrl = baseUrl + "/api/install/file/" + versionId;

    // // A. Nếu là Android -> Redirect thẳng tới file APK
    // if (userAgent.contains("Android")) {
    // return ResponseEntity.status(302).header(HttpHeaders.LOCATION,
    // proxyFileUrl).build();
    // }

    // // B. Nếu là iOS (iPhone/iPad) -> Dùng giao thức itms-services
    // if (userAgent.contains("iPhone") || userAgent.contains("iPad") ||
    // userAgent.contains("iPod")) {
    // // Link tới file plist (Cũng phải dùng base-url HTTPS)
    // String plistUrl = baseUrl + "/api/install/plist/" + versionId;

    // // Giao thức cài đặt của Apple
    // String itmsUrl = "itms-services://?action=download-manifest&url=" + plistUrl;

    // return ResponseEntity.status(302).header(HttpHeaders.LOCATION,
    // itmsUrl).build();
    // }

    // // C. Nếu là máy tính hoặc khác -> Tải file luôn
    // return ResponseEntity.status(302).header(HttpHeaders.LOCATION,
    // proxyFileUrl).build();
    // }

    // Sửa trực tiếp trong hàm này
    @GetMapping("/{versionId}")
    public ResponseEntity<?> installApp(@PathVariable Long versionId,
            @RequestHeader(HttpHeaders.USER_AGENT) String userAgent) {

        // --- DEBUG: Ghi log xem nó nhận diện là gì ---
        System.out.println("DEBUG User-Agent: " + userAgent);

        // --- CẤU HÌNH CỨNG LINK CLOUDFLARE CỦA BẠN (Thay đúng link vào đây) ---
        String myBaseUrl = "https://eye-partial-mattress-marijuana.trycloudflare.com";
        // (Lấy link từ ảnh bạn gửi, nhớ kiểm tra lại xem Cloudflare có đổi chưa nhé)

        // 1. Logic ép buộc trả về giao thức cài đặt cho iPhone (Không cần check
        // UserAgent nữa để test)
        String plistUrl = myBaseUrl + "/api/install/plist/" + versionId;
        String itmsUrl = "itms-services://?action=download-manifest&url=" + plistUrl;

        System.out.println("DEBUG ITMS URL: " + itmsUrl);

        return ResponseEntity.status(302).header(HttpHeaders.LOCATION, itmsUrl).build();
    }

    // --- 2. API PHỤC VỤ FILE PLIST (CHO IOS) ---
    @GetMapping(value = "/plist/{versionId}", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getPlist(@PathVariable Long versionId) {
        AppVersion version = appVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version not found"));

        // Link file IPA trong plist cũng phải là link Proxy HTTPS
        String proxyFileUrl = baseUrl + "/api/install/file/" + versionId;

        String plistContent = iosPlistService.generatePlist(
                proxyFileUrl,
                version.getApp().getPackageName(),
                version.getVersion(),
                version.getApp().getName());

        return ResponseEntity.ok(plistContent);
    }

    // --- 3. API PROXY (TẢI FILE TỪ MINIO VỀ CHO CLIENT) ---
    // API này giúp giấu MinIO đi, Client chỉ cần biết link Cloudflare là tải được
    @GetMapping("/file/{versionId}")
    public void downloadFileProxy(@PathVariable Long versionId, HttpServletResponse response) {
        try {
            AppVersion version = appVersionRepository.findById(versionId)
                    .orElseThrow(() -> new RuntimeException("Version not found"));

            // Xác định tên file (dựa vào đường dẫn cũ hoặc mặc định)
            String fileName = "app-install";
            if (version.getFileUrl().endsWith(".ipa"))
                fileName += ".ipa";
            else if (version.getFileUrl().endsWith(".apk"))
                fileName += ".apk";
            else
                fileName += ".zip"; // Mặc định

            // Cấu hình Header để trình duyệt hiểu đây là file cần tải
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            // Lấy luồng dữ liệu từ MinIO
            InputStream minioStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(version.getFileUrl())
                            .build());

            // Copy dữ liệu từ MinIO sang Response của User
            // (Dùng transferTo của Java 9+ rất tiện)
            minioStream.transferTo(response.getOutputStream());
            response.flushBuffer();
            minioStream.close();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tải file từ MinIO: " + e.getMessage());
        }
    }
}