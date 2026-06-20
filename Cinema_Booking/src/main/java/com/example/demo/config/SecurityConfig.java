package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Bật tính năng phân quyền bằng Annotation (@PreAuthorize) tại tầng Controller nếu cần
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 1. Ai cũng có thể truy cập các trang này
                .requestMatchers("/register", "/login", "/css/**", "/js/**").permitAll()
                
                // 2. Phân quyền nghiêm ngặt theo vai trò (Role)
                // Lưu ý: .hasRole("ADMIN") tự hiểu là cần Authority dạng "ROLE_ADMIN" (đã cấu hình ở CustomUserDetailsService)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN") // Nhân viên hoặc Admin đều vào được
                
                // 3. Khách hàng và các vai trò trên sau khi login đều vào được các vùng còn lại
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true) // Luồng điều hướng thông minh sau khi đăng nhập thành công
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                // Nếu User cố tình vào trang không có quyền, đá về trang 403 (Access Denied)
                .accessDeniedPage("/403")
            );

        return http.build();
    }
}