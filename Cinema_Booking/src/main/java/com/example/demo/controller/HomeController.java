package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class HomeController {

    private final UserRepository userRepository;

    public HomeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String viewHomePage(Model model) {
        // 1. Lấy thông tin xác thực từ Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 2. Kiểm tra xem người dùng đã thực sự đăng nhập chưa (Loại trừ khách vãng lai anonymous)
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            
            String currentUsername = auth.getName();
            
            // Chỉ khi đã đăng nhập thành công mới truy vấn DB để lấy Object User đưa ra View
            Optional<User> userOpt = userRepository.findByUsername(currentUsername);
            userOpt.ifPresent(user -> model.addAttribute("currentUser", user));
            
        } else {
            // Nếu chưa đăng nhập, đảm bảo thuộc tính này bằng null để Thymeleaf không bị nhận diện sai
            model.addAttribute("currentUser", null);
        }
     
        return "index";
    }

    @GetMapping("/403")
    public String viewAccessDeniedPage() {
        return "403";
    }
}