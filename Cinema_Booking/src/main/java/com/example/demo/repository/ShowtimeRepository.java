package com.example.demo.repository;

import com.example.demo.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    // Tìm tất cả suất chiếu thuộc một phòng cụ thể
    List<Showtime> findByRoomRoomId(Long roomId);
}