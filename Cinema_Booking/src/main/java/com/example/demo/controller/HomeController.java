package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Collection;
import java.util.Optional;

@Controller
public class HomeController {

    private final UserRepository userRepository;

    public HomeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String viewHomePage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Optional<User> userOpt = userRepository.findByUsername(currentUsername);
        if (userOpt.isPresent()) {
            model.addAttribute("currentUser", userOpt.get());
        }

        // Kiểm tra danh sách quyền (Authorities) của User hiện tại để truyền trạng thái sang View
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isStaff = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isStaff", isStaff);

        return "index";
    }

    // Tạo thêm router xử lý trang báo lỗi từ chối truy cập 403
    @GetMapping("/403")
    public String viewAccessDeniedPage() {
        return "403";
    }
}