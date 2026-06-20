package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserManagementController {

    private final UserService userService;

    public AdminUserManagementController(UserService userService) {
        this.userService = userService;
    }
    // ... Các hàm listUsers và editUser nằm ở đây ...
    // 1. Hiển thị danh sách toàn bộ tài khoản
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/user-list"; // Trỏ vào thư mục templates/admin/user-list.html
    }

    // 2. Tiếp nhận dữ liệu chỉnh sửa tài khoản từ Form điền nhanh (Quick Edit)
    @PostMapping("/edit")
    public String editUser(@RequestParam("id") Long id,
                           @RequestParam("fullName") String fullName,
                           @RequestParam("email") String email,
                           @RequestParam("phone") String phone,
                           @RequestParam("role") String role,
                           @RequestParam(value = "enabled", defaultValue = "false") Boolean enabled) {
        try {
            userService.adminUpdateUser(id, fullName, email, phone, role, enabled);
            return "redirect:/admin/users?success=true";
        } catch (RuntimeException e) {
            return "redirect:/admin/users?error=" + e.getMessage();
        }
    }
    
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        try {
            userService.deleteUser(id);
            return "redirect:/admin/users?deleted=true"; 
        } catch (RuntimeException e) {
            return "redirect:/admin/users?error=" + e.getMessage();
        }
    }
}