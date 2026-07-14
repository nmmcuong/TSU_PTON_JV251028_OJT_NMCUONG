package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BookingService;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller xử lý luồng đặt vé: thanh toán, xem lịch sử, hủy vé, hóa đơn.
 *
 * Prefix URL: /booking
 */
@Controller
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    /**
     * Xử lý form thanh toán vé (POST từ trang chọn ghế).
     * CORE-06: Gọi Service có @Transactional + Pessimistic Locking.
     */
    @PostMapping("/checkout")
    public String processBooking(@RequestParam("showtimeId") Long showtimeId,
                                 @RequestParam("seats") List<String> selectedSeats,
                                 @RequestParam("paymentMethod") String paymentMethod,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {

        // 1. Lấy thông tin user đang đăng nhập (hỗ trợ cả OAuth2 và form login)
        User currentUser = resolveCurrentUser(auth);
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // 2. Gọi service xử lý đặt vé — @Transactional bên trong
            Booking successfulBooking = bookingService.createBooking(
                    currentUser.getId(),
                    showtimeId,
                    selectedSeats,
                    paymentMethod
            );

            // 3. Thành công → Redirect đến trang hóa đơn
            redirectAttributes.addFlashAttribute("successMessage", "🎉 Đặt vé thành công!");
            return "redirect:/booking/invoice/" + successfulBooking.getBookingId();

        } catch (RuntimeException e) {
            // 4. Có ghế bị tranh chấp (đã Rollback) → Về lại trang chọn ghế với thông báo lỗi
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/showtime/" + showtimeId + "/seats";
        }
    }

    /**
     * Hiển thị trang hóa đơn xác nhận sau khi đặt vé thành công.
     * CORE-07: JOIN đầy đủ thông tin phim + suất chiếu + ghế.
     */
    @GetMapping("/invoice/{id}")
    public String viewInvoice(@PathVariable("id") Long bookingId,
                               Authentication auth,
                               Model model) {

        // 1. Tìm booking theo ID
        Optional<Booking> bookingOpt = bookingService.getBookingById(bookingId);
        if (bookingOpt.isEmpty()) {
            return "redirect:/booking/history";
        }

        Booking booking = bookingOpt.get();

        // 2. Kiểm tra quyền: Chỉ chủ sở hữu booking hoặc Admin mới được xem
        User viewer = resolveCurrentUser(auth);
        boolean isOwner = viewer != null && booking.getUser().getId().equals(viewer.getId());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            return "redirect:/403";
        }

        // 3. Phân tích danh sách ghế từ chuỗi "A1,A2,B3" → mảng
        String[] seatList = {};
        if (booking.getBookingSeatArray() != null && !booking.getBookingSeatArray().isEmpty()) {
            seatList = booking.getBookingSeatArray().split(",");
        }

        model.addAttribute("booking", booking);
        model.addAttribute("seatList", seatList);

        return "booking-invoice";
    }

    /**
     * Hiển thị lịch sử đặt vé của người dùng đang đăng nhập.
     * CORE-07: JOIN đầy đủ bookings + showtimes + movies + ghế.
     */
    @GetMapping("/history")
    public String viewBookingHistory(Model model, Authentication auth) {

        // Nếu chưa đăng nhập → đá về login
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        User currentUser = resolveCurrentUser(auth);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Gọi service JOIN đầy đủ thông tin
        List<Booking> historyList = bookingService.getBookingHistory(currentUser.getId());

        model.addAttribute("historyList", historyList);
        model.addAttribute("currentUser", currentUser);

        return "booking-history";
    }

    /**
     * Xử lý yêu cầu hủy vé.
     * CORE-09: Chỉ hủy được trước 24h so với giờ chiếu.
     * Ghế được giải phóng ngay khi hủy (status → CANCELLED).
     *
     * FIX: Mapping đúng là /cancel/{id} (không phải /booking/cancel/{id})
     * vì class đã có @RequestMapping("/booking")
     */
    @PostMapping("/cancel/{id}")
    public String handleCancelTicket(@PathVariable("id") Long bookingId,
                                     Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        try {
            String result = bookingService.cancelTicket(bookingId, auth.getName());

            if ("QUÁ_MUỘN".equals(result)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "❌ Hủy vé thất bại! Bạn chỉ có thể hủy trước giờ chiếu 24 tiếng.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "✅ Hủy vé thành công! Ghế của bạn đã được giải phóng.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi xảy ra khi hủy vé: " + e.getMessage());
        }

        return "redirect:/booking/history";
    }

    /**
     * Helper: Lấy User hiện tại, hỗ trợ cả đăng nhập thông thường và OAuth2 (Google/Facebook).
     */
    private User resolveCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        // OAuth2 (Google / Facebook)
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