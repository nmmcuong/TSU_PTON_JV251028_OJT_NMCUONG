package com.example.demo.config;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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

    // Inject bằng interface để tránh type mismatch với setUserDetailsService()
    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CẤU HÌNH QUAN TRỌNG: Kết nối CustomUserDetailsService + BCrypt vào Spring Security.
     * Spring Security 6.x: DaoAuthenticationProvider yêu cầu truyền UserDetailsService vào constructor.
     * Thiếu bean này, Spring Security KHÔNG thể xác minh mật khẩu BCrypt khi đăng nhập.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // Spring Security 6.x: dùng constructor có tham số thay vì no-arg + setter
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        // Chỉ định thuật toán hash để so sánh mật khẩu (BCrypt)
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Đăng ký AuthenticationProvider vào SecurityFilterChain
        http.authenticationProvider(authenticationProvider());

        http
        .authorizeHttpRequests(auth -> auth
    	    // CHÍ MẠNG: Cho phép TRANG CHỦ "/" truy cập tự do không cần login, kèm các trang tĩnh và auth
    	    .requestMatchers("/", "/register", "/login", "/css/**", "/js/**", "/images/**").permitAll()
    	    
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
                .failureUrl("/login?error=true") // Redirect khi sai mật khẩu
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                // Nếu User cố tình vào trang không có quyền, đá về trang 403 (Access Denied)
                .accessDeniedPage("/403")
            );

        return http.build();
    }
}