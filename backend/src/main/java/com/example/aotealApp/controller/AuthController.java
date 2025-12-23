package com.example.aotealApp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aotealApp.dto.JwtAuthenticationResponse;
import com.example.aotealApp.dto.LoginRequest;
import com.example.aotealApp.dto.SignUpRequest;
import com.example.aotealApp.repository.RoleRepository;
import com.example.aotealApp.repository.UserRepository;
import com.example.aotealApp.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpRequest signUpRequest) {
        // 1. Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Lỗi: Username đã được sử dụng!");
        }

        // 2. Tạo user mới
        User user = new User();
        user.setFullName(signUpRequest.getFullName());
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        // 3. Gán quyền mặc định là USER
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy Role User."));
        user.setRoles(Collections.singleton(userRole));

        userRepository.save(user);

        return ResponseEntity.ok("Đăng ký thành công! Hãy đăng nhập ngay.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        // 1. Xác thực username/password
        // AuthenticationManager sẽ tự động gọi CustomUserDetailsService để lấy user ra
        // so sánh password (đã mã hóa)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        // 2. Nếu không có lỗi (đăng nhập đúng), set thông tin vào Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Tạo JWT Token trả về cho client
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }
}