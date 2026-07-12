package com.example.demo.service;

import com.example.demo.enums.BookingStatus;
import com.example.demo.model.Booking;
import com.example.demo.model.Showtime;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.ShowtimeRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * Triển khai nghiệp vụ đặt vé, hủy vé và tra cứu lịch sử.
 * CORE-06: Tất cả thao tác ghi đều được bọc trong @Transactional.
 */
@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              ShowtimeRepository showtimeRepository,
                              UserRepository userRepository,
                              EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.showtimeRepository = showtimeRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * CORE-06: Tạo đơn đặt vé với Pessimistic Locking + @Transactional.
     * Nếu ghế đã bị người khác đặt → ném Exception → Rollback toàn bộ.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Booking createBooking(Long userId, Long showtimeId, List<String> selectedSeats, String paymentMethod) {

        // 1. Khóa dòng showtime (Pessimistic Write Lock) để độc chiếm xử lý
        Showtime showtime = showtimeRepository.findByIdForBooking(showtimeId)
                .orElseThrow(() -> new RuntimeException("Lịch chiếu không tồn tại hoặc đã bị hủy."));

        // 2. Lấy tất cả ghế đã được đặt THÀNH CÔNG của lịch chiếu này
        List<Booking> activeBookings = bookingRepository
                .findByShowtimeShowtimeIdAndBookingStatusNot(showtimeId, BookingStatus.FAILED);

        Set<String> bookedSeatsPool = new HashSet<>();
        for (Booking b : activeBookings) {
            if (b.getBookingSeatArray() != null && !b.getBookingSeatArray().isEmpty()) {
                // Tách chuỗi "A1,A2" thành mảng và đưa vào tập hợp đối chiếu
                String[] seats = b.getBookingSeatArray().split(",");
                bookedSeatsPool.addAll(Arrays.asList(seats));
            }
        }

        // 3. Đối chiếu ghế người dùng muốn đặt với ghế đã bán
        for (String seat : selectedSeats) {
            if (bookedSeatsPool.contains(seat.trim())) {
                // Ghế đã bị chiếm → Ném exception → Rollback toàn bộ transaction
                throw new RuntimeException("Ghế " + seat + " vừa có người khác nhanh tay đặt mất! Vui lòng chọn ghế khác.");
            }
        }

        // 4. Tất cả ghế còn trống → Tiến hành tạo booking
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Tài khoản người dùng không hợp lệ."));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setBookingDate(LocalDateTime.now());
        if ("CASH".equalsIgnoreCase(paymentMethod)) {
            booking.setBookingStatus(BookingStatus.CONFIRMED);
        } else {
            booking.setBookingStatus(BookingStatus.PENDING);
        }

        // Gộp danh sách ghế thành chuỗi TEXT (VD: "A1,A2,B3")
        String seatArrayString = String.join(",", selectedSeats);
        booking.setBookingSeatArray(seatArrayString);

        // Tính tổng tiền: số lượng ghế × đơn giá suất chiếu
        double totalPrice = showtime.getPrice() * selectedSeats.size();
        booking.setTotalAmount(BigDecimal.valueOf(totalPrice));

        // Lưu xuống DB trong cùng một transaction
        Booking savedBooking = bookingRepository.save(booking);

        // Kích hoạt gửi email bất đồng bộ (chạy ngầm trên thread khác)
        emailService.sendBookingConfirmationEmail(savedBooking);

        return savedBooking;
    }

    /**
     * CORE-07: Lấy lịch sử đặt vé với đầy đủ thông tin JOIN.
     */
    @Override
    public List<Booking> getBookingHistory(Long userId) {
        return bookingRepository.findBookingHistoryByUserId(userId);
    }

    /**
     * CORE-09: Hủy vé trước 24h so với giờ chiếu.
     * @Transactional đảm bảo cập nhật status và giải phóng ghế là 1 đơn vị.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String cancelTicket(Long bookingId, String username) {

        // 1. Tìm đơn vé thuộc về đúng người dùng đang đăng nhập
        Optional<Booking> bookingOptional = bookingRepository.findByBookingIdAndUserUsername(bookingId, username);

        if (bookingOptional.isEmpty()) {
            return "QUÁ_MUỘN"; // Không tìm thấy đơn → không cho hủy
        }

        Booking booking = bookingOptional.get();

        // 2. Kiểm tra đơn có đang ở trạng thái CONFIRMED không
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            return "QUÁ_MUỘN"; // Đơn đã bị hủy hoặc thất bại trước đó
        }

        // 3. Kiểm tra khoảng cách thời gian còn lại so với giờ chiếu
        LocalDateTime showtimeStart = booking.getShowtime().getStartTime();
        LocalDateTime now = LocalDateTime.now();
        long hoursLeft = Duration.between(now, showtimeStart).toHours();

        if (hoursLeft < 24) {
            // Cách giờ chiếu chưa đến 24 tiếng → Không cho hủy
            return "QUÁ_MUỘN";
        }

        // 4. Đủ điều kiện → Đổi trạng thái sang CANCELLED
        // CORE-09: Ghế tự động "giải phóng" vì status CANCELLED không được tính là ghế đã bán
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        return "THÀNH_CÔNG";
    }

    /**
     * Lấy chi tiết booking theo ID — dùng cho trang Invoice.
     */
    @Override
    public Optional<Booking> getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }
}