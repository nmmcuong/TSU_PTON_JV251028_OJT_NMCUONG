package com.example.demo.repository;

import com.example.demo.model.Room;
import com.example.demo.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository để truy vấn dữ liệu ghế ngồi theo phòng chiếu.
 * Được dùng chủ yếu trong trang chọn ghế để hiển thị sơ đồ.
 */
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    /**
     * Lấy tất cả ghế của một phòng chiếu cụ thể, sắp xếp theo mã ghế
     * để hiển thị đúng thứ tự A1, A2... B1, B2...
     */
    List<Seat> findByRoomOrderBySeatCodeAsc(Room room);

    /**
     * Kiểm tra phòng đã có ghế chưa (dùng khi seed data để tránh tạo trùng)
     */
    boolean existsByRoom(Room room);
}
