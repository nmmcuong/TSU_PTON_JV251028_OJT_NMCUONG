package com.example.demo.service;

import com.example.demo.model.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Dịch vụ gửi Email bất đồng bộ (CORE-03 Hướng 3).
 * `@Async` giúp chạy phương thức này trên một Thread Pool riêng biệt,
 * không làm nghẽn luồng xử lý chính (Request Thread) của khách hàng.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    /**
     * Gửi email xác nhận đặt vé chứa mã QR code giả định.
     * Phương thức này sẽ chạy bất đồng bộ trên một Thread riêng biệt.
     *
     * @param booking Đối tượng Booking chứa thông tin đơn hàng
     */
    @Async
    public void sendBookingConfirmationEmail(Booking booking) {
        String threadName = Thread.currentThread().getName();
        log.info("[ASYNC EMAIL] Bắt đầu xử lý gửi email cho đơn hàng #{} trên Thread: [{}]", 
                booking.getBookingId(), threadName);

        try {
            // Giả lập thời gian kết nối SMTP Server và sinh mã QR Code (mất khoảng 3 giây)
            Thread.sleep(3000);

            String qrCodeData = "SMARTCINEMA-B" + booking.getBookingId() + "-" + booking.getUser().getUsername();

            log.info("\n" +
                    "========================================= EMAIL SENT =========================================\n" +
                    "To: {}\n" +
                    "Subject: 🎉 Xác nhận đặt vé thành công - Đơn hàng #{}\n" +
                    "----------------------------------------------------------------------------------------------\n" +
                    "Xin chào {},\n" +
                    "Chúc mừng bạn đã đặt vé thành công tại Smart Cinema!\n\n" +
                    "Thông tin vé của bạn:\n" +
                    "  - Phim: {}\n" +
                    "  - Suất chiếu: {}\n" +
                    "  - Phòng chiếu: {}\n" +
                    "  - Ghế ngồi: {}\n" +
                    "  - Tổng tiền: {} đ\n\n" +
                    "Mã QR Code nhận vé tại quầy (Sinh tự động):\n" +
                    "  [QR CODE DATA]: {}\n" +
                    "  [MÃ SỐ]: {}\n\n" +
                    "Hẹn gặp lại bạn tại rạp chiếu phim!\n" +
                    "==============================================================================================",
                    booking.getUser().getEmail(),
                    booking.getBookingId(),
                    booking.getUser().getFullName(),
                    booking.getShowtime().getMovie().getTitle(),
                    booking.getShowtime().getStartTime(),
                    booking.getShowtime().getRoom().getRoomName(),
                    booking.getBookingSeatArray(),
                    booking.getTotalAmount(),
                    qrCodeData,
                    "SC-" + booking.getBookingId()
            );

            log.info("[ASYNC EMAIL] Gửi email thành công cho đơn hàng #{} trên Thread: [{}]", 
                    booking.getBookingId(), threadName);

        } catch (InterruptedException e) {
            log.error("[ASYNC EMAIL] Lỗi khi đang giả lập gửi email: ", e);
            Thread.currentThread().interrupt();
        }
    }
}
