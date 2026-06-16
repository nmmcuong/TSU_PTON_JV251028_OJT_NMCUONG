package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.example.demo.enums.Role; // Import enum từ package enums của em

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 100)
    private String email;

    // --- Các thông tin cá nhân (Gộp từ UserProfile sang) ---
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(length = 20)
    private String gender; // Có thể để String hoặc tạo Enum Gender riêng tùy em

    private LocalDate birthday;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    // --- Các thông tin quản lý tài khoản ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private Boolean enabled;

    @Column(name = "disable_reason", length = 255)
    private String disableReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Mối quan hệ với bảng Đặt Vé (Bookings) ---
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude // Loại trừ trường này khỏi hàm toString tự động để chống lỗi StackOverflow
    private List<Booking> bookings;
}