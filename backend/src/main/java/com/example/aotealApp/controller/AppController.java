package com.example.aotealApp.controller;

import java.util.List;

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

import com.example.aotealApp.dto.AppDTO;
import com.example.aotealApp.dto.AppDetailDTO;
import com.example.aotealApp.entity.AppVersion;
import com.example.aotealApp.entity.VersionStatus;
import com.example.aotealApp.services.AppService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
@CrossOrigin("*") // Cho phép Frontend gọi API (tạm thời mở hết)
public class AppController {

    private final AppService appService;

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

}
