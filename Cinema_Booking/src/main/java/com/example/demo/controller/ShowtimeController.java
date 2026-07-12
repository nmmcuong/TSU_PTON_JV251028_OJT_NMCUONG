package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.model.Seat;
import com.example.demo.model.Showtime;
import com.example.demo.repository.SeatRepository;
import com.example.demo.service.ShowtimeService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller xử lý luồng chọn ghế trước khi thanh toán.
 * URL: /showtime/{id}/seats
 *
 * Luồng: Xem phim → Chọn suất chiếu → [Trang này] Chọn ghế → Thanh toán
 */
@Controller
@RequestMapping("/showtime")
public class ShowtimeController {

    private final ShowtimeService showtimeService;
    private final SeatRepository seatRepository;

    public ShowtimeController(ShowtimeService showtimeService, SeatRepository seatRepository) {
        this.showtimeService = showtimeService;
        this.seatRepository = seatRepository;
    }

    /**
     * Hiển thị sơ đồ ghế cho một suất chiếu cụ thể.
     *
     * Model attributes truyền ra View:
     * - showtime   : Đối tượng suất chiếu (phim + phòng + giờ + giá)
     * - seatMap    : Map<String hàng, List<Seat>> — ghế nhóm theo hàng để render grid
     * - bookedSeats: Set<String> — mã các ghế đã bán (để tô màu đỏ)
     * - isSoldOut  : Boolean — cờ báo hiệu phòng đã hết vé
     *
     * CORE-08: Nếu suất chiếu đã qua giờ → redirect về trang chủ
     */
    @GetMapping("/{id}/seats")
    public String showSeatSelection(@PathVariable("id") Long showtimeId,
                                    Model model,
                                    Authentication auth) {

        // 1. Lấy thông tin suất chiếu — ném 404 nếu không tồn tại
        Showtime showtime = showtimeService.getShowtimeById(showtimeId);

        // 2. CORE-08: Không cho chọn ghế nếu suất đã bắt đầu
        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            return "redirect:/?error=expired";
        }

        // 3. Lấy danh sách tất cả ghế của phòng chiếu
        Room room = showtime.getRoom();
        List<Seat> allSeats = seatRepository.findByRoomOrderBySeatCodeAsc(room);

        // 4. Nhóm ghế theo hàng (A, B, C...) → dễ render thành grid HTML
        // Dùng LinkedHashMap để giữ thứ tự hàng A → Z
        Map<String, List<Seat>> seatMap = new LinkedHashMap<>();
        for (Seat seat : allSeats) {
            // Tách hàng từ mã ghế: "A1" → "A", "B10" → "B"
            String rowKey = seat.getSeatCode().replaceAll("[0-9]", "");
            seatMap.computeIfAbsent(rowKey, k -> new java.util.ArrayList<>()).add(seat);
        }

        // 5. Lấy tập hợp mã ghế đã bán (để tô màu trên sơ đồ)
        Set<String> bookedSeats = showtimeService.getBookedSeatCodes(showtimeId);

        // 6. Kiểm tra phòng có hết vé không
        boolean isSoldOut = bookedSeats.size() >= room.getTotalSeats();

        // 7. Đẩy dữ liệu ra View
        model.addAttribute("showtime", showtime);
        model.addAttribute("seatMap", seatMap);
        model.addAttribute("bookedSeats", bookedSeats);
        model.addAttribute("isSoldOut", isSoldOut);

        return "seat-selection";
    }
}
