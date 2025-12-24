package com.example.aotealApp.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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
import com.example.aotealApp.services.AppService;
import com.example.aotealApp.services.MinioStorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
@CrossOrigin("*") // Cho phép Frontend gọi API (tạm thời mở hết)
public class AppController {

    private final AppService appService;
    private final AppSearchRepository appSearchRepository;
    private final AppVersionRepository appVersionRepository;
    private final AppRepository appRepository;
    private final MinioStorageService storageService;

    // API Upload: POST /api/apps/upload
    // Dùng form-data để gửi file và text
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadApp(
            @RequestParam("name") String name,
            @RequestParam("packageName") String packageName,
            @RequestParam("description") String description,
            @RequestParam("version") String version,
            @RequestParam("releaseNote") String releaseNote,
            @RequestPart("file") MultipartFile file) {
        try {
            AppVersion newVersion = appService.uploadNewAppVersion(name, packageName, description, version, releaseNote,
                    file);
            return ResponseEntity.ok(newVersion);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<AppDTO>> getAllApps() {
        return ResponseEntity.ok(appService.getAllApps());
    }

    // API: Lấy danh sách các App đang chờ duyệt (Chỉ Admin/Approver thấy)
    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_APPROVER')")
    public ResponseEntity<List<AppDTO>> getPendingApps() {
        return ResponseEntity.ok(appService.getAppsByStatus(VersionStatus.PENDING_APPROVAL));
    }

    // API: Duyệt hoặc Từ chối App
    @PostMapping("/{versionId}/approve")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_APPROVER')")
    public ResponseEntity<?> approveApp(@PathVariable Long versionId, @RequestParam boolean isApproved) {
        try {
            appService.approveApp(versionId, isApproved);
            return ResponseEntity.ok("Đã xử lý thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Lấy chi tiết App và lịch sử version
    @GetMapping("/{appId}")
    public ResponseEntity<AppDetailDTO> getAppDetail(@PathVariable Long appId) {
        return ResponseEntity.ok(appService.getAppDetail(appId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<AppDTO>> searchApps(@RequestParam String query) {
        // 1. Tìm trong Elastic (Tên hoặc Mô tả chứa từ khóa)
        List<AppDocument> docs = appSearchRepository.findByNameContainingOrDescriptionContaining(query, query);

        if (docs.isEmpty()) {
            return ResponseEntity.ok(List.of()); // Trả về rỗng nếu không tìm thấy
        }

        // 2. Lấy danh sách ID của các App tìm được
        List<Long> appIds = docs.stream()
                .map(AppDocument::getId)
                .collect(Collectors.toList());

        // 3. Query DB để lấy thông tin chi tiết (Phiên bản mới nhất đã Publish) của các
        // App này
        // Lưu ý: Logic này giả định mỗi App chỉ hiển thị 1 bản mới nhất ra kết quả tìm
        // kiếm
        List<AppDTO> results = appVersionRepository.findAll().stream()
                .filter(v -> appIds.contains(v.getApp().getId())) // Chỉ lấy app có trong list tìm kiếm
                .filter(v -> v.getStatus() == VersionStatus.PUBLISHED) // Chỉ lấy bản đã duyệt
                // Group by App ID và lấy bản mới nhất (nếu DB có nhiều version published)
                .collect(Collectors.groupingBy(v -> v.getApp().getId()))
                .values().stream()
                .map(versions -> {
                    // Lấy version có ID lớn nhất (mới nhất) trong nhóm
                    AppVersion latest = versions.stream()
                            .max((v1, v2) -> v1.getId().compareTo(v2.getId()))
                            .orElseThrow();

                    // Map sang DTO
                    AppDTO dto = new AppDTO();
                    dto.setId(latest.getApp().getId()); // ID của App (để click vào detail)
                    dto.setName(latest.getApp().getName());
                    dto.setDescription(latest.getApp().getDescription());
                    dto.setLatestVersion(latest.getVersion());
                    dto.setUpdatedAt(latest.getCreatedAt());
                    dto.setDownloadUrl(storageService.getPresignedUrl(latest.getFileUrl()));
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    @PostMapping("/sync-elastic")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')") // Chỉ admin chạy
    public ResponseEntity<?> syncData() {
        // 1. Lấy tất cả App từ DB
        List<App> allApps = appRepository.findAll();

        // 2. Convert sang Document và lưu vào Elastic
        List<AppDocument> docs = allApps.stream().map(app -> {
            AppDocument doc = new AppDocument();
            doc.setId(app.getId());
            doc.setName(app.getName());
            doc.setDescription(app.getDescription());
            doc.setPackageName(app.getPackageName());
            doc.setStatus("PUBLISHED"); // Tạm thời set cứng, thực tế nên check version
            return doc;
        }).collect(Collectors.toList());

        appSearchRepository.saveAll(docs);

        return ResponseEntity.ok("Đã đồng bộ " + docs.size() + " ứng dụng sang Elasticsearch!");
    }

}
