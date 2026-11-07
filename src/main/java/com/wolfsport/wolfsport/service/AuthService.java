package com.wolfsport.wolfsport.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wolfsport.wolfsport.dto.RegisterForm;
import com.wolfsport.wolfsport.entity.User;
import com.wolfsport.wolfsport.repository.UserRepository;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Trả về user nếu đăng nhập đúng, ngược lại trả về rỗng.
     * Lý do thất bại được ghi ở console (không hiển thị cho người dùng) để dễ gỡ lỗi.
     */
    public Optional<User> login(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            return Optional.empty();
        }
        User user = userRepository.findByEmail(email.trim()).orElse(null);
        if (user == null) {
            log.warn("Đăng nhập thất bại: không có tài khoản với email '{}'", email.trim());
            return Optional.empty();
        }
        String stored = user.getPassword();
        if (stored == null || !stored.startsWith("$2")) {
            log.warn("Đăng nhập thất bại: mật khẩu của '{}' trong DB KHÔNG phải mã BCrypt "
                    + "(phải bắt đầu bằng $2a$ hoặc $2b$). Hãy cập nhật lại bằng mã BCrypt.", user.getEmail());
            return Optional.empty();
        }
        if (!passwordEncoder.matches(rawPassword, stored)) {
            log.warn("Đăng nhập thất bại: sai mật khẩu của '{}'", user.getEmail());
            return Optional.empty();
        }
        if (user.getStatus() == null || !User.STATUS_ACTIVE.equalsIgnoreCase(user.getStatus().trim())) {
            log.warn("Đăng nhập thất bại: tài khoản '{}' có status = '{}' (cần là ACTIVE)",
                    user.getEmail(), user.getStatus());
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    /** Đăng ký tài khoản khách hàng mới (mật khẩu được mã hóa BCrypt). */
    public User register(RegisterForm form) {
        User user = new User();
        user.setFullName(form.getFullName().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setPhone(blankToNull(form.getPhone()));
        user.setAddress(blankToNull(form.getAddress()));
        user.setRole(User.ROLE_CUSTOMER); // luôn là khách hàng, không ai tự đăng ký làm ADMIN được
        user.setStatus(User.STATUS_ACTIVE);
        return userRepository.save(user);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
