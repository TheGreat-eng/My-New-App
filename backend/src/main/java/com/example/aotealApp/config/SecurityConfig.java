package com.example.aotealApp.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.aotealApp.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Cho phép dùng @PreAuthorize trên từng hàm
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Mã hóa password
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF vì dùng JWT
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Không
                                                                                                              // dùng
                                                                                                              // Session
                .authorizeHttpRequests(auth -> auth
                        // 1. API Public (Ai cũng vào được)
                        .requestMatchers("/api/auth/**").permitAll() // Login/Register
                        .requestMatchers(HttpMethod.GET, "/api/apps/**").permitAll() // Xem danh sách App

                        // SỬA: Cho phép OPTIONS request (preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Upload cần xác thực
                        .requestMatchers("/api/apps/upload").hasAnyAuthority("ROLE_ADMIN", "ROLE_DEV")

                        // .requestMatchers(HttpMethod.POST, "/api/apps/upload").authenticated()

                        // 3. Các request còn lại phải đăng nhập
                        .anyRequest().authenticated());

        // Thêm filter JWT vào trước filter mặc định
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // QUAN TRỌNG: Thêm header cho multipart
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "x-auth-token",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight request

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}