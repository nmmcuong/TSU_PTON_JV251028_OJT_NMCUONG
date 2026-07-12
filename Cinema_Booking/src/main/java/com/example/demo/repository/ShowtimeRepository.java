package com.example.demo.repository;

import com.example.demo.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.demo.model.Showtime;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    // Tìm tất cả suất chiếu thuộc một phòng cụ thể
    List<Showtime> findByRoomRoomId(Long roomId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Showtime s WHERE s.showtimeId = :id")
    Optional<Showtime> findByIdForBooking(@Param("id") Long id);
    List<Showtime> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime now);
    List<Showtime> findByMovieMovieIdAndStartTimeAfterOrderByStartTimeAsc(Long movieId, LocalDateTime now);
}