package com.example.aotealApp.repository;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.example.aotealApp.document.AppDocument;

public interface AppSearchRepository extends ElasticsearchRepository<AppDocument, Long> {
    // Tìm kiếm theo tên HOẶC mô tả (có chứa từ khóa)
    List<AppDocument> findByNameContainingOrDescriptionContaining(String name, String description);

    // Tìm chính xác status
    List<AppDocument> findByStatus(String status);
}
