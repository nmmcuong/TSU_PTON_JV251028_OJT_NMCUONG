package com.example.demo.service;

import com.example.demo.enums.BookingStatus;
import com.example.demo.model.Booking;
import com.example.demo.model.Showtime;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.ShowtimeRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = Exception.class) // Đảm bảo tính toàn vẹn: có lỗi là hủy toàn bộ
    public Booking createBooking(Long userId, Long showtimeId, List<String> selectedSeats, String paymentMethod) {
        
        // 1. Tìm và KHÓA dòng lịch chiếu lại để độc chiếm quyền xử lý luồng đặt ghế này
        Showtime showtime = showtimeRepository.findByIdForBooking(showtimeId)
                .orElseThrow(() -> new RuntimeException("Lịch chiếu không tồn tại hoặc đã bị hủy."));

        // 2. Lấy danh sách tất cả các ghế đã được đặt thành công của lịch chiếu này
        List<Booking> activeBookings = bookingRepository.findByShowtimeShowtimeIdAndBookingStatusNot(showtimeId, BookingStatus.FAILED);
        
        Set<String> bookedSeatsPool = new HashSet<>();
        for (Booking b : activeBookings) {
            if (b.getBookingSeatArray() != null && !b.getBookingSeatArray().isEmpty()) {
                // Cắt chuỗi "A1,A2" thành mảng và đưa vào tập hợp để đối chiếu
                String[] seats = b.getBookingSeatArray().split(",");
                bookedSeatsPool.addAll(Arrays.asList(seats));
            }
        }

        // 3. Đối chiếu danh sách ghế người dùng đang chọn với kho ghế đã bán
        for (String seat : selectedSeats) {
            if (bookedSeatsPool.contains(seat)) {
                // [Alt: Có ghế vừa bị người khác mua mất] -> Ném Exception để hệ thống hủy bỏ (Rollback) hoàn toàn
                throw new RuntimeException("Ghế " + seat + " vừa có người khác nhanh tay đặt mất. Vui lòng chọn ghế khác!");
            }
        }

        // 4. [Alt: Tất cả ghế đều trống] -> Tiến hành tạo đơn hàng và lưu dữ liệu đồng thời
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Tài khoản người dùng không hợp lệ."));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setBookingDate(LocalDateTime.now());
        booking.setPaymentMethod(paymentMethod);
        booking.setBookingStatus(BookingStatus.CONFIRMED); // Hoặc PENDING tùy luồng thanh toán

        // Gộp mảng danh sách ghế người dùng chọn thành chuỗi TEXT (Ví dụ: "A1,A2")
        String seatArrayString = String.join(",", selectedSeats);
        booking.setBookingSeatArray(seatArrayString);

        // Tính tổng tiền dựa trên số lượng ghế nhân với đơn giá lịch chiếu
        double totalPrice = showtime.getPrice() * selectedSeats.size();
        booking.setTotalAmount(BigDecimal.valueOf(totalPrice));

        // Lưu xuống DB (Dữ liệu đơn và danh sách ghế được lưu đồng thời trên 1 dòng)
        return bookingRepository.save(booking);
    }
    
    @Override
    public List<Booking> getBookingHistory(Long userId) {
        // Gọi Repository để lấy danh sách đã được JOIN sẵn phim và suất chiếu
        return bookingRepository.findBookingHistoryByUserId(userId);
    }
    
}