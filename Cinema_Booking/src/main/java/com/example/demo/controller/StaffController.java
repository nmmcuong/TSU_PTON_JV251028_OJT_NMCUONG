package com.example.demo.controller;

import com.example.demo.enums.BookingStatus;
import com.example.demo.model.Booking;
import com.example.demo.repository.BookingRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controller dành riêng cho nhân viên rạp (ROLE_STAFF).
 * Tính năng: Tìm kiếm vé, xem chi tiết vé, xác nhận soát vé, in vé.
 */
@Controller
@RequestMapping("/staff")
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
public class StaffController {

    private final BookingRepository bookingRepository;

    public StaffController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Trang chính: Tìm kiếm vé theo mã booking, tên khách, hoặc email.
     */
    @GetMapping("/tickets/check")
    public String ticketCheckPage(@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                                  Model model) {
        List<Booking> results = Collections.emptyList();
        if (!keyword.isBlank()) {
            results = bookingRepository.searchByKeyword(keyword.trim());
        }
        model.addAttribute("keyword", keyword);
        model.addAttribute("results", results);
        return "staff/ticket-check";
    }

    /**
     * Xem chi tiết vé theo mã booking ID — trang in vé.
     */
    @GetMapping("/tickets/{id}")
    public String ticketDetail(@PathVariable("id") Long bookingId, Model model) {
        Optional<Booking> bookingOpt = bookingRepository.findByIdWithDetails(bookingId);
        if (bookingOpt.isEmpty()) {
            return "redirect:/staff/tickets/check?error=not_found";
        }
        Booking booking = bookingOpt.get();
        String[] seatList = booking.getBookingSeatArray() != null
                ? booking.getBookingSeatArray().split(",") : new String[]{};
        model.addAttribute("booking", booking);
        model.addAttribute("seatList", seatList);
        return "staff/ticket-detail";
    }

    /**
     * Xác nhận soát vé — đánh dấu vé đã được kiểm tra (status → USED).
     */
    @PostMapping("/tickets/{id}/confirm")
    public String confirmTicket(@PathVariable("id") Long bookingId,
                                RedirectAttributes redirectAttributes) {
        Optional<Booking> bookingOpt = bookingRepository.findByIdWithDetails(bookingId);
        if (bookingOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy vé #" + bookingId);
            return "redirect:/staff/tickets/check";
        }
        Booking booking = bookingOpt.get();
        if (booking.getBookingStatus() == BookingStatus.USED) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "⚠️ Vé #" + bookingId + " đã được soát trước đó rồi!");
            return "redirect:/staff/tickets/" + bookingId;
        }
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "❌ Vé #" + bookingId + " đã bị hủy, không thể soát!");
            return "redirect:/staff/tickets/" + bookingId;
        }
        booking.setBookingStatus(BookingStatus.USED);
        bookingRepository.save(booking);
        redirectAttributes.addFlashAttribute("successMessage",
                "✅ Soát vé thành công! Vé #" + bookingId + " đã được xác nhận.");
        return "redirect:/staff/tickets/" + bookingId;
    }
}