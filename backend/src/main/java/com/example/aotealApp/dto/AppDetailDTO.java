package com.example.aotealApp.dto;

import java.util.List;

import lombok.Data;

@Data
public class AppDetailDTO {
    private Long id;
    private String name;
    private String description;
    private String packageName;
    private String iconUrl;

    // Danh sách các version của app này
    private List<AppDTO> versions;
}
