package com.example.demo.service;
import com.example.demo.dto.UserRegisterDto;
import com.example.demo.model.User;
import com.example.demo.enums.Role;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerNewUser(UserRegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        // Băm mật khẩu bằng BCrypt trước khi lưu vào DB
        user.setPassword(passwordEncoder.encode(registerDto.getPassword())); 
        user.setEmail(registerDto.getEmail());
        user.setFullName(registerDto.getFullName());
        user.setPhone(registerDto.getPhone());
        user.setAddress(registerDto.getAddress());
        user.setRole(Role.USER); // Mặc định tài khoản mới là USER
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}
