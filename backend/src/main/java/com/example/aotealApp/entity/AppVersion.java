package com.example.aotealApp.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "app_versions")
@Data
public class AppVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    private String version; // 1.0.0

    private String fileUrl; // Đường dẫn file trên MinIO (quan trọng)

    private Long fileSize; // Dung lượng (byte)

    @Enumerated(EnumType.STRING)
    private VersionStatus status; // DRAFT, PENDING, PUBLISHED, DEPRECATED

    private String releaseNote; // Có gì mới?

    @CreationTimestamp // <--- Thêm cái này
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
}
