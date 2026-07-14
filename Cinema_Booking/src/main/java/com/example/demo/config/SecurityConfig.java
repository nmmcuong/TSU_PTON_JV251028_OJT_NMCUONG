package com.example.demo.config;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity 
public class SecurityConfig {

   
    private final UserDetailsService userDetailsService;
    private final com.example.demo.service.CustomOAuth2UserService customOAuth2UserService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserDetailsService userDetailsService,
                          com.example.demo.service.CustomOAuth2UserService customOAuth2UserService,
                          PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.passwordEncoder = passwordEncoder;
    }

    // PasswordEncoder được khai báo tại AppConfig.java để tránh Circular Dependency

   
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);      
        provider.setPasswordEncoder(passwordEncoder);
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
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
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