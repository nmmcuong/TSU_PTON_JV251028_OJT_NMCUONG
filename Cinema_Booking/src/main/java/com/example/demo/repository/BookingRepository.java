package com.example.demo.repository;

import com.example.demo.model.Booking;
import com.example.demo.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Lấy các đơn đặt vé theo lịch chiếu và trạng thái khác FAILED/CANCELLED nếu có
    List<Booking> findByShowtimeShowtimeIdAndBookingStatusNot(Long showtimeId, BookingStatus status);
}