package com.example.aotealApp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aotealApp.entity.App;

public interface AppRepository extends JpaRepository<App, Long> {
    Optional<App> findByPackageName(String packageName);
}
