package com.example.demo.service;

import com.example.demo.enums.BookingStatus;
import com.example.demo.model.Booking;
import com.example.demo.model.Movie;
import com.example.demo.model.Showtime;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.ShowtimeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Triển khai các nghiệp vụ liên quan đến Suất chiếu phim.
 * CORE-05: Kiểm tra xung đột lịch phòng khi tạo suất mới.
 * CORE-08: Tự ẩn suất chiếu đã qua + đánh dấu Sold Out.
 */
@Service
public class ShowtimeServiceImpl implements ShowtimeService {

    // Constructor injection — KHÔNG dùng @Autowired + final cùng lúc (mâu thuẫn)
    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final BookingRepository bookingRepository;

    public ShowtimeServiceImpl(ShowtimeRepository showtimeRepository,
                               MovieRepository movieRepository,
                               BookingRepository bookingRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAll();
    }

    /**
     * CORE-05: Lưu suất chiếu mới — BẮT BUỘC kiểm tra xung đột phòng.
     * Công thức: Suất B bắt đầu >= (Suất A bắt đầu + duration + 15 phút dọn phòng)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveShowtime(Showtime showtime) throws Exception {

        // 1. Lấy thông tin phim (cần duration để tính giờ kết thúc)
        Movie movie = movieRepository.findById(showtime.getMovie().getMovieId())
                .orElseThrow(() -> new Exception("Không tìm thấy phim!"));

        // 2. Tính khoảng thời gian của suất chiếu mới
        LocalDateTime newStart = showtime.getStartTime();
        // Thời gian kết thúc = Giờ bắt đầu + Thời lượng phim + 15 phút dọn phòng
        LocalDateTime newEnd = newStart.plusMinutes(movie.getDuration()).plusMinutes(15);

        // 3. Lấy tất cả suất chiếu hiện có của phòng này
        List<Showtime> existingShowtimes = showtimeRepository.findByRoomRoomId(showtime.getRoom().getRoomId());

        for (Showtime ex : existingShowtimes) {
            // Nếu đang sửa suất chiếu → bỏ qua so sánh với chính nó
            if (showtime.getShowtimeId() != null && showtime.getShowtimeId().equals(ex.getShowtimeId())) {
                continue;
            }

            LocalDateTime exStart = ex.getStartTime();
            LocalDateTime exEnd = exStart.plusMinutes(ex.getMovie().getDuration()).plusMinutes(15);

            // Thuật toán kiểm tra va chạm khoảng thời gian:
            // Hai suất XÉT là TRÙNG nếu: newStart < exEnd VÀ newEnd > exStart
            if (newStart.isBefore(exEnd) && newEnd.isAfter(exStart)) {
                throw new Exception(
                        "⚠️ XUNG ĐỘT LỊCH CHIẾU: Phòng '" + ex.getRoom().getRoomName()
                        + "' đang có lịch chiếu phim '" + ex.getMovie().getTitle()
                        + "' từ " + exStart.toLocalTime() + " đến " + exEnd.toLocalTime()
                        + " (đã tính 15 phút dọn phòng). Vui lòng chọn khung giờ khác."
                );
            }
        }

        // 4. Không trùng lịch → Lưu vào DB
        showtimeRepository.save(showtime);
    }

    @Override
    @Transactional
    public void deleteShowtime(Long id) {
        showtimeRepository.deleteById(id);
    }

    /**
     * CORE-08: Lấy danh sách suất chiếu còn trong tương lai.
     * Tự động loại bỏ các suất đã qua (startTime < now).
     * Đánh dấu isS oldOut = true nếu phòng đã kín ghế.
     */
    @Override
    public List<Showtime> getAvailableShowtimes() {
        // 1. Chỉ lấy suất chiếu chưa diễn ra (startTime > now)
        List<Showtime> activeShowtimes = showtimeRepository
                .findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now());

        // 2. Đánh dấu Sold Out cho từng suất
        for (Showtime showtime : activeShowtimes) {
            Set<String> bookedSeats = getBookedSeatCodes(showtime.getShowtimeId());
            int totalBookedSeats = bookedSeats.size();
            int roomCapacity = showtime.getRoom().getTotalSeats();

            if (totalBookedSeats >= roomCapacity) {
                showtime.setSoldOut(true);
            }
        }

        return activeShowtimes;
    }

    /**
     * Lấy chi tiết một suất chiếu theo ID.
     */
    @Override
    public Showtime getShowtimeById(Long id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu có ID: " + id));
    }

    /**
     * Lấy tập hợp mã ghế đã được đặt thành công cho một suất chiếu.
     * Dùng để tô màu ghế "Đã bán" trên sơ đồ chọn ghế.
     *
     * Chỉ tính các booking có trạng thái CONFIRMED (bỏ qua FAILED + CANCELLED).
     */
    @Override
    public Set<String> getBookedSeatCodes(Long showtimeId) {
        // Lấy tất cả booking của suất này, trừ trạng thái FAILED (đã rollback)
        List<Booking> activeBookings = bookingRepository
                .findByShowtimeShowtimeIdAndBookingStatusNot(showtimeId, BookingStatus.FAILED);

        Set<String> bookedSeats = new HashSet<>();
        for (Booking b : activeBookings) {
            // Bỏ qua booking đã hủy — ghế của booking CANCELLED được giải phóng
            if (b.getBookingStatus() == BookingStatus.CANCELLED) {
                continue;
            }
            if (b.getBookingSeatArray() != null && !b.getBookingSeatArray().isEmpty()) {
                String[] seats = b.getBookingSeatArray().split(",");
                for (String seat : seats) {
                    bookedSeats.add(seat.trim());
                }
            }
        }
        return bookedSeats;
    }

    @Override
    public List<Showtime> getAvailableShowtimesForMovie(Long movieId) {
        // 1. Chỉ lấy suất chiếu chưa diễn ra của bộ phim này
        List<Showtime> activeShowtimes = showtimeRepository
                .findByMovieMovieIdAndStartTimeAfterOrderByStartTimeAsc(movieId, LocalDateTime.now());

        // 2. Đánh dấu Sold Out cho từng suất
        for (Showtime showtime : activeShowtimes) {
            Set<String> bookedSeats = getBookedSeatCodes(showtime.getShowtimeId());
            int totalBookedSeats = bookedSeats.size();
            int roomCapacity = showtime.getRoom().getTotalSeats();

            if (totalBookedSeats >= roomCapacity) {
                showtime.setSoldOut(true);
            }
        }

        return activeShowtimes;
    }
}