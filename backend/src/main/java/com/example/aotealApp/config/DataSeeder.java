package com.example.aotealApp.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.aotealApp.entity.Role;
import com.example.aotealApp.entity.User;
import com.example.aotealApp.repository.RoleRepository;
import com.example.aotealApp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra nếu chưa có admin thì tạo mới
        if (!userRepository.existsByUsername("admin_sys")) {
            User admin = new User();
            admin.setUsername("admin_sys");
            admin.setPassword(passwordEncoder.encode("123456")); // <--- Mã hóa password ở đây
            admin.setFullName("System Administrator");
            admin.setEmail("admin@example.com");

            // Gán quyền ADMIN và DEV
            Set<Role> roles = new HashSet<>();
            roleRepository.findByName("ROLE_ADMIN").ifPresent(roles::add);
            roleRepository.findByName("ROLE_DEV").ifPresent(roles::add);
            admin.setRoles(roles);

            userRepository.save(admin);
            System.out.println("---- ĐÃ TẠO USER ADMIN MẪU: admin_sys / 123456 ----");
        }
    }
}
