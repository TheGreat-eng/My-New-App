package com.example.aotealApp.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AppDTO {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String latestVersion; // v1.0.0
    private String downloadUrl; // Link tải (Presigned)
    private LocalDateTime updatedAt;
}