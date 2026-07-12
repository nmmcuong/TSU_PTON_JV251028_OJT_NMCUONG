package com.example.demo.service;

import com.example.demo.model.Booking;
import java.util.List;
import java.util.Optional;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến Đặt vé.
 */
public interface BookingService {

    /**
     * Xem lịch sử đặt vé của một khách hàng (CORE-07).
     * JOIN đầy đủ bookings + showtimes + movies.
     */
    List<Booking> getBookingHistory(Long userId);

    /**
     * Tạo đơn đặt vé mới (CORE-06).
     * BẮT BUỘC @Transactional: lưu booking + kiểm tra ghế đồng thời.
     * Ném RuntimeException nếu ghế đã bị người khác đặt (Rollback).
     */
    Booking createBooking(Long userId, Long showtimeId, List<String> selectedSeats, String paymentMethod);

    /**
     * Hủy vé đặt trước (CORE-09).
     * Chỉ cho phép hủy trước 24h so với giờ chiếu.
     * @return "THÀNH_CÔNG" | "QUÁ_MUỘN"
     */
    String cancelTicket(Long bookingId, String username);

    /**
     * Lấy chi tiết một đơn đặt vé theo ID.
     * Dùng cho trang hóa đơn xác nhận sau khi đặt vé thành công.
     */
    Optional<Booking> getBookingById(Long bookingId);
}