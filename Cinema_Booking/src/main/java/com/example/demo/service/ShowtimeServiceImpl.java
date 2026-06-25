package com.example.demo.service;

import com.example.demo.model.Movie;
import com.example.demo.model.Showtime;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.ShowtimeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;

    public ShowtimeServiceImpl(ShowtimeRepository showtimeRepository, MovieRepository movieRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
    }

    @Override
    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveShowtime(Showtime showtime) throws Exception {
        // 1. Lấy thông tin thời lượng phim đầy đủ từ DB
        Movie movie = movieRepository.findById(showtime.getMovie().getMovieId())
                .orElseThrow(() -> new Exception("Không tìm thấy phim!"));

        // 2. Tính toán khoảng thời gian của suất chiếu mới định tạo
        LocalDateTime newStart = showtime.getStartTime();
        // Thời gian kết thúc = Giờ bắt đầu + Thời lượng phim + 15 phút dọn phòng
        LocalDateTime newEnd = newStart.plusMinutes(movie.getDuration()).plusMinutes(15);

        // 3. Lấy danh sách các suất chiếu hiện có của phòng này để đối chiếu
        List<Showtime> existingShowtimes = showtimeRepository.findByRoomRoomId(showtime.getRoom().getRoomId());

        for (Showtime ex : existingShowtimes) {
            // Nếu là hành động Sửa, bỏ qua không so sánh với chính nó
            if (showtime.getShowtimeId() != null && showtime.getShowtimeId().equals(ex.getShowtimeId())) {
                continue;
            }

            LocalDateTime exStart = ex.getStartTime();
            LocalDateTime exEnd = exStart.plusMinutes(ex.getMovie().getDuration()).plusMinutes(15);

            // Áp dụng thuật toán kiểm tra va chạm: NewStart < ExEnd VÀ NewEnd > ExStart
            if (newStart.isBefore(exEnd) && newEnd.isAfter(exStart)) {
                throw new Exception("XUNG ĐỘT LỊCH CHIẾU: Phòng '" + ex.getRoom().getRoomName() 
                        + "' đang có lịch chiếu phim '" + ex.getMovie().getTitle() 
                        + "' từ " + exStart.toLocalTime() + " đến " + exEnd.toLocalTime() + " (Đã tính 15p dọn phòng).");
            }
        }

        // 4. Nếu không trùng lịch, tiến hành ghi nhận vào cơ sở dữ liệu
        showtimeRepository.save(showtime);
    }

    @Override
    @Transactional
    public void deleteShowtime(Long id) {
        showtimeRepository.deleteById(id);
    }
}