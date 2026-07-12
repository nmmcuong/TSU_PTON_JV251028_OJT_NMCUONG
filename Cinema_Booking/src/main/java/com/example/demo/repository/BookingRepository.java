package com.example.demo.repository;

import com.example.demo.enums.BookingStatus;
import com.example.demo.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

/**
 * Repository cho Booking entity.
 * Chứa các truy vấn JOIN phức tạp theo yêu cầu CORE-07.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * CORE-07: Lấy lịch sử đặt vé của user với đầy đủ JOIN.
     * JOIN: bookings → showtimes → movies để trả về hóa đơn hoàn chỉnh.
     */
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.showtime s " +
           "JOIN FETCH s.movie m " +
           "WHERE b.user.id = :userId " +
           "ORDER BY b.bookingDate DESC")
    List<Booking> findBookingHistoryByUserId(@Param("userId") Long userId);

    /**
     * Lấy các booking của một suất chiếu, loại trừ trạng thái FAILED.
     * Dùng để kiểm tra ghế đã bán (CORE-06) và Sold Out (CORE-08).
     */
    List<Booking> findByShowtimeShowtimeIdAndBookingStatusNot(Long showtimeId, BookingStatus status);

    /**
     * Tìm booking theo ID và username của người đặt.
     * Dùng khi hủy vé để đảm bảo chỉ chủ sở hữu mới được hủy.
     * FIX: Dùng java.util.Optional (không phải org.hibernate.internal.util.Optional)
     */
    Optional<Booking> findByBookingIdAndUserUsername(Long bookingId, String username);

    /**
     * Tìm tất cả các booking có trạng thái chỉ định và thời gian đặt vé trước một mốc cụ thể.
     * Dùng cho Background Job quét hủy đơn quá hạn thanh toán.
     */
    List<Booking> findByBookingStatusAndBookingDateBefore(BookingStatus status, LocalDateTime dateTime);
}