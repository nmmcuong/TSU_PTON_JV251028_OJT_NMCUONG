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
        	    // CHÍ MẠNG: Cho phép TRANG CHỦ "/" truy cập tự do không cần login, kèm các trang tĩnh và auth
        	    .requestMatchers("/", "/register", "/login", "/css/**", "/js/**").permitAll()
        	    
        	    // Các vùng quản trị nghiêm ngặt giữ nguyên
        	    .requestMatchers("/admin/**").hasRole("ADMIN")
        	    .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN")
        	    
        	    // Các tính năng như Đặt vé, Profile, Thanh toán thì bắt buộc đăng nhập mới được vào
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