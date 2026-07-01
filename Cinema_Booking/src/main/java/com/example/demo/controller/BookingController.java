package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository; 
import com.example.demo.service.BookingService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken; // Thêm import này
import org.springframework.security.core.Authentication; 
import org.springframework.security.core.context.SecurityContextHolder; // Thêm import này
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;
    
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
    
    @GetMapping("/history")
    public String viewBookingHistory(Model model) {
        
        // 1. Lấy thông tin xác thực từ Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Nếu chưa đăng nhập hoặc là khách ẩn danh -> Đá về trang login
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        // 2. Lấy username hiện tại và truy vấn DB để bốc Object User ra
        Optional<User> userOpt = userRepository.findByUsername(auth.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User currentUser = userOpt.get();

        // 3. Gọi service lấy danh sách lịch sử theo đúng ID của User
        List<Booking> historyList = bookingService.getBookingHistory(currentUser.getId());

        // 4. Truyền dữ liệu ra View
        model.addAttribute("historyList", historyList);
        model.addAttribute("currentUser", currentUser); // Truyền thêm để navbar hoặc giao diện dùng nếu cần
        
        return "booking-history"; 
    }
}