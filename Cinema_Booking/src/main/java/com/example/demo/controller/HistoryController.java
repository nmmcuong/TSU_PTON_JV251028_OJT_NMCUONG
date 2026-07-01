package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.model.User;
import com.example.demo.service.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HistoryController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/profile/history")
    public String viewBookingHistory(HttpSession session, Model model) {
        // 1. Kiểm tra trạng thái phiên đăng nhập của người dùng
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login"; // Chưa đăng nhập thì đá về trang login
        }

        // 2. Lấy danh sách lịch sử đặt vé kèm dữ liệu đã được JOIN liên kết
        List<Booking> historyList = bookingService.getBookingHistory(currentUser.getId());

        // 3. Đẩy dữ liệu ra giao diện Thymeleaf
        model.addAttribute("historyList", historyList);
        
        return "booking-history"; // Trả về file template booking-history.html
    }
}