package com.example.demo.config;

import com.example.demo.enums.Role;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Component chạy tự động khi ứng dụng khởi động.
 * Nhiệm vụ: Kiểm tra và SEED dữ liệu ban đầu nếu DB còn trống.
 * Bao gồm: Phòng chiếu, Ghế ngồi, Thể loại phim, Tài khoản Admin.
 *
 * THIẾT KẾ: Dùng ApplicationRunner thay vì @PostConstruct để đảm bảo
 * Spring context đã khởi tạo hoàn toàn trước khi chạy.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoomRepository roomRepository,
                           SeatRepository seatRepository,
                           GenreRepository genreRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roomRepository = roomRepository;
        this.seatRepository = seatRepository;
        this.genreRepository = genreRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== DataInitializer: Bắt đầu kiểm tra và seed dữ liệu ban đầu ===");
        seedGenres();
        seedRoomsAndSeats();
        seedAdminAccount();
        log.info("=== DataInitializer: Hoàn tất seed dữ liệu ===");
    }

    // =====================================================================
    // SEED: THỂ LOẠI PHIM (6 thể loại phổ biến)
    // =====================================================================
    private void seedGenres() {
        if (genreRepository.count() > 0) {
            log.info("  > Genres đã tồn tại, bỏ qua seed.");
            return;
        }
        List<Genre> genres = List.of(
                new Genre("Hành Động"),
                new Genre("Hài Hước"),
                new Genre("Kinh Dị"),
                new Genre("Tình Cảm"),
                new Genre("Hoạt Hình"),
                new Genre("Tài Liệu"),
                new Genre("Khoa Học Viễn Tưởng"),
                new Genre("Phiêu Lưu")
        );
        genreRepository.saveAll(genres);
        log.info("  > Đã seed {} thể loại phim.", genres.size());
    }

    // =====================================================================
    // SEED: PHÒNG CHIẾU + GHẾ (5 phòng, mỗi phòng có sơ đồ ghế riêng)
    // =====================================================================
    private void seedRoomsAndSeats() {
        if (roomRepository.count() > 0) {
            log.info("  > Rooms đã tồn tại, bỏ qua seed.");
            return;
        }

        // --- Phòng 1: Standard (8 hàng x 10 cột = 80 ghế) ---
        Room room1 = new Room();
        room1.setRoomName("Phòng 1 - Standard");
        room1.setTotalSeats(80);
        room1.setSeatsX(10);
        room1.setSeatsY(8);
        room1.setStatus(true);
        roomRepository.save(room1);
        generateSeats(room1, 8, 10, "GHCGHI"); // Hàng G,H: VIP; I: Couple

        // --- Phòng 2: VIP (6 hàng x 8 cột = 48 ghế) ---
        Room room2 = new Room();
        room2.setRoomName("Phòng 2 - VIP");
        room2.setTotalSeats(48);
        room2.setSeatsX(8);
        room2.setSeatsY(6);
        room2.setStatus(true);
        roomRepository.save(room2);
        generateSeats(room2, 6, 8, "EF");   // Hàng E,F: VIP

        // --- Phòng 3: IMAX (10 hàng x 12 cột = 120 ghế) ---
        Room room3 = new Room();
        room3.setRoomName("Phòng 3 - IMAX");
        room3.setTotalSeats(120);
        room3.setSeatsX(12);
        room3.setSeatsY(10);
        room3.setStatus(true);
        roomRepository.save(room3);
        generateSeats(room3, 10, 12, "HIJJ"); // Hàng H,I: VIP; J: Couple

        // --- Phòng 4: 4DX (5 hàng x 8 cột = 40 ghế) ---
        Room room4 = new Room();
        room4.setRoomName("Phòng 4 - 4DX");
        room4.setTotalSeats(40);
        room4.setSeatsX(8);
        room4.setSeatsY(5);
        room4.setStatus(true);
        roomRepository.save(room4);
        generateSeats(room4, 5, 8, "DE");   // Hàng D,E: VIP

        // --- Phòng 5: Premium (7 hàng x 10 cột = 70 ghế) ---
        Room room5 = new Room();
        room5.setRoomName("Phòng 5 - Premium");
        room5.setTotalSeats(70);
        room5.setSeatsX(10);
        room5.setSeatsY(7);
        room5.setStatus(true);
        roomRepository.save(room5);
        generateSeats(room5, 7, 10, "FGH"); // Hàng F,G: VIP; H: Couple

        log.info("  > Đã seed 5 phòng chiếu với đầy đủ sơ đồ ghế.");
    }

    /**
     * Tạo toàn bộ ghế cho một phòng chiếu.
     *
     * @param room       Phòng chiếu cần tạo ghế
     * @param numRows    Số hàng ghế (8 = hàng A đến H)
     * @param numCols    Số cột ghế (10 = cột 1 đến 10)
     * @param vipRowCodes Chuỗi các ký tự hàng VIP/Couple (VD: "GH" = hàng G và H là VIP)
     *                   Ký tự cuối nếu là chữ thường = Couple (quy ước nội bộ)
     *
     * Logic phân loại ghế:
     * - 2 hàng cuối trong vipRowCodes (nếu có chữ thường) → COUPLE
     * - Các hàng còn lại trong vipRowCodes → VIP
     * - Tất cả hàng khác → NORMAL
     */
    private void generateSeats(Room room, int numRows, int numCols, String vipRowCodes) {
        // Tránh tạo ghế trùng lặp nếu chạy lại (idempotent)
        if (seatRepository.existsByRoom(room)) {
            return;
        }

        for (int row = 0; row < numRows; row++) {
            // Hàng ghế: A=0, B=1, C=2...
            char rowChar = (char) ('A' + row);
            String rowLetter = String.valueOf(rowChar);

            for (int col = 1; col <= numCols; col++) {
                String seatCode = rowLetter + col; // VD: "A1", "B10"

                // Xác định loại ghế dựa trên vị trí hàng
                String seatType = determineSeatType(rowChar, numRows, vipRowCodes);

                Seat seat = new Seat(room, seatCode, seatType);
                seatRepository.save(seat);
            }
        }
        log.info("    > Đã tạo {} ghế cho {}", numRows * numCols, room.getRoomName());
    }

    /**
     * Xác định loại ghế dựa trên vị trí hàng trong phòng.
     * Quy tắc: 2 hàng cuối → VIP, hàng cuối cùng → COUPLE, còn lại → NORMAL
     */
    private String determineSeatType(char rowChar, int numRows, String vipRowCodes) {
        // Hàng cuối cùng: luôn là ghế đôi (COUPLE)
        char lastRow = (char) ('A' + numRows - 1);
        if (rowChar == lastRow && vipRowCodes != null && !vipRowCodes.isEmpty()) {
            return "COUPLE";
        }

        // Hàng áp chót: VIP
        char secondLastRow = (char) ('A' + numRows - 2);
        if (rowChar == secondLastRow && vipRowCodes != null && !vipRowCodes.isEmpty()) {
            return "VIP";
        }

        return "NORMAL";
    }

    // =====================================================================
    // SEED: TÀI KHOẢN ADMIN MẶC ĐỊNH
    // =====================================================================
    private void seedAdminAccount() {
        // Kiểm tra tài khoản admin đã tồn tại chưa
        if (userRepository.existsByUsername("admin")) {
            log.info("  > Tài khoản admin đã tồn tại, bỏ qua seed.");
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        // CORE-01: Mật khẩu BẮT BUỘC hash BCrypt trước khi lưu DB
        admin.setPassword(passwordEncoder.encode("Admin@123456"));
        admin.setEmail("admin@smartcinema.vn");
        admin.setFullName("Quản Trị Viên Hệ Thống");
        admin.setPhone("0900000000");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        userRepository.save(admin);

        // --- Tài khoản Staff mẫu ---
        if (!userRepository.existsByUsername("staff01")) {
            User staff = new User();
            staff.setUsername("staff01");
            staff.setPassword(passwordEncoder.encode("Staff@123456"));
            staff.setEmail("staff01@smartcinema.vn");
            staff.setFullName("Nhân Viên Quầy Vé");
            staff.setPhone("0911111111");
            staff.setRole(Role.STAFF);
            staff.setEnabled(true);
            staff.setCreatedAt(LocalDateTime.now());
            staff.setUpdatedAt(LocalDateTime.now());
            userRepository.save(staff);
        }

        // --- Tài khoản Customer mẫu để test ---
        if (!userRepository.existsByUsername("customer01")) {
            User customer = new User();
            customer.setUsername("customer01");
            customer.setPassword(passwordEncoder.encode("Customer@123"));
            customer.setEmail("customer01@gmail.com");
            customer.setFullName("Nguyễn Văn A");
            customer.setPhone("0922222222");
            customer.setRole(Role.CUSTOMER);
            customer.setEnabled(true);
            customer.setCreatedAt(LocalDateTime.now());
            customer.setUpdatedAt(LocalDateTime.now());
            userRepository.save(customer);
        }

        log.info("  > Đã seed tài khoản: admin / staff01 / customer01");
        log.info("  > Mật khẩu admin: Admin@123456 | staff: Staff@123456 | customer: Customer@123");
    }
}
