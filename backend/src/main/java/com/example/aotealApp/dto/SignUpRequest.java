package com.example.aotealApp.dto;

import lombok.Data;

@Data
public class SignUpRequest {
    private String fullName;
    private String username;
    private String email;
    private String password;
}
