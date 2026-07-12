package com.example.demo.controller;

import com.example.demo.dto.UserRegisterDto;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Xử lý các route xác thực: đăng nhập và đăng ký.
 *
 * LƯU Ý: Không có @PostMapping("/login") ở đây vì
 * Spring Security tự xử lý POST /login thông qua UsernamePasswordAuthenticationFilter.
 * Controller chỉ cần cung cấp trang hiển thị GET /login.
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // -------------------------------------------------------------------
    // ĐĂNG NHẬP: Spring Security tự xử lý POST, chỉ cần GET để show form
    // -------------------------------------------------------------------
    @GetMapping("/login")
    public String viewLoginPage() {
        return "login";
    }

    // -------------------------------------------------------------------
    // ĐĂNG KÝ
    // -------------------------------------------------------------------
    @GetMapping("/register")
    public String viewRegisterPage(Model model) {
        // Cung cấp object rỗng để Thymeleaf binding không bị lỗi
        model.addAttribute("userDto", new UserRegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String performRegistration(@Valid @ModelAttribute("userDto") UserRegisterDto registerDto,
                                      BindingResult result, Model model) {
        // 1. Kiểm tra các lỗi Validation (username quá ngắn, email sai định dạng...)
        if (result.hasErrors()) {
            // Spring tự đặt lại model, không cần add lại userDto
            return "register";
        }

        try {
            // 2. Gọi Service tạo tài khoản mới (password tự động được hash BCrypt)
            userService.registerNewUser(registerDto);

            // 3. Thành công → Chuyển về trang Login kèm thông báo
            return "redirect:/login?success=true";

        } catch (RuntimeException e) {
            // 4. Thất bại (trùng username / email) → Hiển thị lỗi trên form
            // ĐÃ SỬA: đặt vào key "error" để khớp với th:if="${error}" trong template
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}