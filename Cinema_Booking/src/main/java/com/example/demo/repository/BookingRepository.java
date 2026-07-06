package com.example.demo.repository;

import com.example.demo.model.Booking;
import com.example.demo.enums.BookingStatus;

import org.hibernate.internal.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
	@Query("SELECT b FROM Booking b " +
	           "JOIN FETCH b.showtime s " +
	           "JOIN FETCH s.movie m " +
	           "WHERE b.user.id = :userId " +
	           "ORDER BY b.bookingDate DESC")
	    List<Booking> findBookingHistoryByUserId(@Param("userId") Long userId);
    // Lấy các đơn đặt vé theo lịch chiếu và trạng thái khác FAILED/CANCELLED nếu có
    List<Booking> findByShowtimeShowtimeIdAndBookingStatusNot(Long showtimeId, BookingStatus status);
    List<Booking> findByShowtimeIdAndBookingStatus(Long showtimeId, String status);
    Optional<Booking> findByBookingIdAndUserUsername(Long bookingId, String username);
}