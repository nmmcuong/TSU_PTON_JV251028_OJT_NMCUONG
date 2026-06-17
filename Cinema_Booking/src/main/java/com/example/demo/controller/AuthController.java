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

@Controller
// XÓA DÒNG @RequiredArgsConstructor NẾU CÒN
public class AuthController {

    private final UserService userService;

    // TỰ VIẾT CONSTRUCTOR THỦ CÔNG Ở ĐÂY:
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String viewLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String viewRegisterPage(Model model) {
        model.addAttribute("userDto", new UserRegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String performRegistration(@Valid @ModelAttribute("userDto") UserRegisterDto registerDto,
                                      BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerNewUser(registerDto);
            return "redirect:/login?success=true";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }
}