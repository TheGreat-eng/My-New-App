package com.example.aotealApp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "apps")
@Data
public class App {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Ví dụ: HR Portal Mobile

    @Column(columnDefinition = "TEXT")
    private String description;

    private String iconUrl; // Link icon trên MinIO
    
    private String packageName; // com.viettel.hr (Định danh duy nhất)

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy; // Ai là người tạo app này
}
