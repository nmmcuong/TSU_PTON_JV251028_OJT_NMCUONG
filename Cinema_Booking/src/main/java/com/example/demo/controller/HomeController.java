package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Optional;

@Controller
public class HomeController {

    private final UserRepository userRepository;

    // Khởi tạo Constructor thủ công cho STS
    public HomeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String viewHomePage(Model model) {
        // 1. Lấy tên đăng nhập (username) của người vừa login thành công từ Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        // 2. Tìm thông tin chi tiết trong Database bằng username
        Optional<User> userOpt = userRepository.findByUsername(currentUsername);
        if (userOpt.isPresent()) {
            // Truyền đối tượng user sang file html
            model.addAttribute("currentUser", userOpt.get());
        }

        return "index"; // Trỏ đến file index.html trong thư mục templates
    }
}