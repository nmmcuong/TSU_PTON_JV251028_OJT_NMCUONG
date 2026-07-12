package com.example.demo.service;

import com.example.demo.model.Showtime;
import java.util.List;
import java.util.Set;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến Suất chiếu phim.
 */
public interface ShowtimeService {

    /** Lấy tất cả suất chiếu (dùng cho Admin) */
    List<Showtime> getAllShowtimes();

    /**
     * Lưu hoặc cập nhật suất chiếu.
     * CORE-05: Kiểm tra xung đột phòng trước khi lưu.
     * @throws Exception nếu phòng bị trùng lịch
     */
    void saveShowtime(Showtime showtime) throws Exception;

    /** Xóa suất chiếu theo ID */
    void deleteShowtime(Long id);

    /**
     * Lấy các suất chiếu còn trong tương lai (CORE-08).
     * Tự động loại bỏ suất đã qua và đánh dấu suất hết vé (Sold Out).
     */
    List<Showtime> getAvailableShowtimes();

    /**
     * Lấy chi tiết một suất chiếu theo ID.
     * Dùng cho trang chọn ghế.
     */
    Showtime getShowtimeById(Long id);

    /**
     * Lấy tập hợp mã ghế đã được đặt THÀNH CÔNG cho một suất chiếu.
     * Dùng để hiển thị ghế đã bán trên sơ đồ chọn ghế.
     *
     * @param showtimeId ID suất chiếu
     * @return Set<String> các mã ghế đã bán, VD: {"A1", "A2", "B5"}
     */
    Set<String> getBookedSeatCodes(Long showtimeId);

    /**
     * Lấy danh sách suất chiếu khả dụng cho một phim cụ thể (CORE-08).
     * Loại bỏ suất đã qua và check trạng thái Sold Out.
     */
    List<Showtime> getAvailableShowtimesForMovie(Long movieId);
}