package com.example.demo.scheduler;

import com.example.demo.enums.BookingStatus;
import com.example.demo.model.Booking;
import com.example.demo.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Background Job / Cron Job định kỳ kiểm tra và xử lý trạng thái vé.
 * Lập lịch chạy ngầm thực thi mỗi 1 phút (CORE-03 Hướng 3).
 */
@Component
public class BookingScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingScheduler.class);

    private final BookingRepository bookingRepository;

    public BookingScheduler(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Cron Job mỗi 1 phút (chạy ngầm): Tự động hủy các booking ở trạng thái PENDING (CHỜ_THANH_TOÁN)
     * đã quá 15 phút mà chưa hoàn tất thanh toán.
     *
     * Biểu thức cron chạy cứ mỗi phút một lần.
     */
    @Scheduled(cron = "0 */1 * * * *")
    @Transactional(rollbackFor = Exception.class)
    public void autoCancelExpiredBookings() {
        log.info("[CRON JOB] Bắt đầu quét các đơn đặt vé quá hạn 15 phút chưa thanh toán...");

        // Mốc thời gian giới hạn = hiện tại - 15 phút
        LocalDateTime expirationThreshold = LocalDateTime.now().minusMinutes(15);

        // Tìm tất cả booking PENDING được tạo trước thời điểm expirationThreshold
        List<Booking> expiredBookings = bookingRepository.findByBookingStatusAndBookingDateBefore(
                BookingStatus.PENDING, 
                expirationThreshold
        );

        if (expiredBookings.isEmpty()) {
            log.info("[CRON JOB] Không tìm thấy đơn hàng nào quá hạn thanh toán.");
            return;
        }

        log.info("[CRON JOB] Phát hiện {} đơn hàng quá hạn thanh toán. Tiến hành hủy đơn...", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            // Chuyển trạng thái sang FAILED (Thanh toán thất bại) hoặc CANCELLED (Đã hủy)
            // CORE-09: Khi trạng thái chuyển sang FAILED hoặc CANCELLED, các ghế của đơn này 
            // sẽ tự động được giải phóng cho người dùng khác đặt.
            booking.setBookingStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);

            log.info("  > Đơn hàng #{} (Người dùng: {}, Tổng tiền: {} đ, Đặt lúc: {}) đã bị hủy tự động.",
                    booking.getBookingId(),
                    booking.getUser().getUsername(),
                    booking.getTotalAmount(),
                    booking.getBookingDate()
            );
        }

        log.info("[CRON JOB] Hoàn tất xử lý hủy đơn quá hạn thanh toán.");
    }
}
