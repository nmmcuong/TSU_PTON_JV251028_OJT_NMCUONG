package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.model.User;
import com.example.demo.service.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/checkout")
    public String processBooking(@RequestParam("showtimeId") Long showtimeId,
                                 @RequestParam("seats") List<String> selectedSeats,
                                 @RequestParam("paymentMethod") String paymentMethod,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        
        // Kiểm tra trạng thái phiên đăng nhập của người dùng
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thực hiện đặt vé!");
            return "redirect:/login";
        }

        try {
            // Thực thi nghiệp vụ xử lý lõi đặt vé
            Booking successfulBooking = bookingService.createBooking(
                    currentUser.getId(), 
                    showtimeId, 
                    selectedSeats, 
                    paymentMethod
            );

            // Báo thành công và hiện hóa đơn sang trang xác nhận đơn hàng
            redirectAttributes.addFlashAttribute("successMessage", "Đặt vé thành công!");
            return "redirect:/booking/invoice/" + successfulBooking.getBookingId();

        } catch (RuntimeException e) {
            // Bắt lỗi trùng ghế (Rollback thành công) -> Trả thông báo lỗi cụ thể về màn hình chọn ghế
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/showtime/" + showtimeId + "/seats"; 
        }
    }
}