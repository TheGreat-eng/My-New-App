package com.example.aotealApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aotealApp.entity.AppVersion;

public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {

}
