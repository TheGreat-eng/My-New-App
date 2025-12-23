package com.example.aotealApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aotealApp.entity.AppVersion;
import com.example.aotealApp.entity.VersionStatus;

public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {
    // Tìm tất cả version có status = PUBLISHED
    List<AppVersion> findByStatus(VersionStatus status);
}
