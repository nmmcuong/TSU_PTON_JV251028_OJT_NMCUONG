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
    
 // --- 1. Không có tham số (No-Args Constructor) ---
    public User() {
    }

    // --- 2. Có đầy đủ tham số (All-Args Constructor) ---
    public User(Long id, String username, String password, String email, String fullName, String phone, 
                String address, String gender, LocalDate birthday, String avatarUrl, Role role, 
                Boolean enabled, String disableReason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.gender = gender;
        this.birthday = birthday;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.enabled = enabled;
        this.disableReason = disableReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- 3. Toàn bộ các hàm Getter và Setter thủ công ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // ĐÂY CHÍNH LÀ HÀM ĐANG BỊ BÁO THIẾU:
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDisableReason() {
        return disableReason;
    }

    public void setDisableReason(String disableReason) {
        this.disableReason = disableReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
}