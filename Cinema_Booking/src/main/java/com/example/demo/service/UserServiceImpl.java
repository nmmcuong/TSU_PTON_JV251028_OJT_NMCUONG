package com.example.demo.service;

import com.example.demo.dto.UserProfileDto;
import com.example.demo.dto.UserRegisterDto;
import com.example.demo.model.User;
import com.example.demo.enums.Role;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Triển khai các nghiệp vụ quản lý tài khoản người dùng.
 * CORE-01: Mật khẩu BẮT BUỘC hash BCrypt trước khi lưu DB.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * CORE-01: Đăng ký tài khoản mới.
     * Mật khẩu PHẢI được hash bằng BCrypt trước khi lưu.
     * Không bao giờ lưu plain text password.
     */
    @Override
    @Transactional
    public User registerNewUser(UserRegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng bởi tài khoản khác!");
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        // CORE-01: Hash mật khẩu bằng BCrypt — TUYỆT ĐỐI không lưu plain text
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setEmail(registerDto.getEmail());
        user.setFullName(registerDto.getFullName());
        user.setPhone(registerDto.getPhone());
        user.setAddress(registerDto.getAddress());
        // Mặc định tài khoản mới đăng ký là CUSTOMER (không phải USER)
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * CORE-03: Cập nhật thông tin cá nhân của người dùng.
     */
    @Override
    @Transactional
    public User updateUserProfile(String username, UserProfileDto profileDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));

        // Kiểm tra email mới có trùng với tài khoản khác không
        if (!user.getEmail().equals(profileDto.getEmail())
                && userRepository.existsByEmail(profileDto.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng bởi tài khoản khác!");
        }

        user.setFullName(profileDto.getFullName());
        user.setEmail(profileDto.getEmail());
        user.setPhone(profileDto.getPhone());
        user.setAddress(profileDto.getAddress());
        user.setGender(profileDto.getGender());
        user.setBirthday(profileDto.getBirthday());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Admin cập nhật thông tin tài khoản bất kỳ (bao gồm cả role và enabled).
     */
    @Override
    @Transactional
    public void adminUpdateUser(Long id, String fullName, String email, String phone, String role, Boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng có ID: " + id));

        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email này đã được sử dụng bởi tài khoản khác!");
        }

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(Role.valueOf(role)); // Ép chuỗi "ADMIN"/"STAFF"/"CUSTOMER" → Enum
        user.setEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    /**
     * Soft Delete: Vô hiệu hóa tài khoản (không xóa cứng khỏi DB).
     * Giữ lại lịch sử đặt vé và các dữ liệu liên quan.
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản để xử lý!"));

        // Soft delete: chuyển enabled = false thay vì xóa cứng
        user.setEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    /**
     * @deprecated Phương thức này KHÔNG còn được dùng.
     * Spring Security đã xử lý xác thực thông qua CustomUserDetailsService + BCrypt.
     * Giữ lại để tương thích với interface — không gọi trực tiếp.
     */
    @Override
    @Deprecated
    public User authenticate(String username, String password) {
        // Spring Security xử lý xác thực — method này không nên được gọi
        throw new UnsupportedOperationException(
                "Xác thực phải thực hiện qua Spring Security, không phải method này!"
        );
    }
}
