package com.example.demo.service;
import com.example.demo.dto.UserProfileDto;
import com.example.demo.dto.UserRegisterDto;
import com.example.demo.model.User;
import com.example.demo.enums.Role;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

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
    
    @Override
    @Transactional
    public User updateUserProfile(String username, UserProfileDto profileDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));

        // Kiểm tra nếu người dùng đổi email sang một email khác đã tồn tại trong hệ thống
        if (!user.getEmail().equals(profileDto.getEmail()) && userRepository.existsByEmail(profileDto.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng bởi tài khoản khác!");
        }

        // Tiến hành cập nhật thông tin mới vào Model
        user.setFullName(profileDto.getFullName());
        user.setEmail(profileDto.getEmail());
        user.setPhone(profileDto.getPhone());
        user.setAddress(profileDto.getAddress());
        user.setGender(profileDto.getGender());
        user.setBirthday(profileDto.getBirthday());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user); // Lưu xuống Database
    }
    
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    @Transactional
    public void adminUpdateUser(Long id, String fullName, String email, String phone, String role, Boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng có ID: " + id));

        // Kiểm tra trùng email (nếu email bị thay đổi)
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email này đã được sử dụng bởi tài khoản khác!");
        }

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(com.example.demo.enums.Role.valueOf(role)); // Ép chuỗi String thành Enum
        user.setEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
    }

//    @Override
//    @Transactional
//    public void deleteUser(Long id) {
//        if (!userRepository.existsById(id)) {
//            throw new RuntimeException("Không tìm thấy tài khoản để xóa!");
//        }
//        userRepository.deleteById(id);
//    }
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản để xử lý!"));
        
        // Chuyển đổi trạng thái hoạt động về false (0)
        user.setEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user); // Cập nhật lại xuống Database
    }
}
