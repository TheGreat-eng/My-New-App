package com.example.aotealApp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.aotealApp.dto.AppDTO;
import com.example.aotealApp.entity.AppVersion;
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
}
