package com.example.demo.config;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    public GlobalControllerAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        // Trường hợp đăng nhập bằng Google / Facebook (OAuth2)
        if (auth instanceof OAuth2AuthenticationToken) {
            OAuth2User oAuth2User = (OAuth2User) auth.getPrincipal();
            String email = (String) oAuth2User.getAttributes().get("email");
            if (email != null) {
                return userRepository.findByEmail(email).orElse(null);
            }
            return null;
        }

        // Form login thông thường
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }
}