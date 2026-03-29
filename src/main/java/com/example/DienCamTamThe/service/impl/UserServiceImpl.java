package com.example.DienCamTamThe.service.impl;

import com.example.DienCamTamThe.dto.request.LoginRequest;
import com.example.DienCamTamThe.dto.request.RegisterRequest;
import com.example.DienCamTamThe.dto.response.ApiResponse;
import com.example.DienCamTamThe.entity.User;
import com.example.DienCamTamThe.exception.UserAlreadyExistsException;
import com.example.DienCamTamThe.repository.UserRepository;
import com.example.DienCamTamThe.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ApiResponse<String> register(RegisterRequest request) {
        // Kiểm tra xem username đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username '" + request.getUsername() + "' already exists");
        }

        // Tạo Entity User từ DTO
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Mã hóa password

        // Lưu user
        userRepository.save(user);

        return ApiResponse.success("User registered successfully");
    }

    @Override
    public ApiResponse<String> login(LoginRequest request) {
        // Tìm user theo username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // Kiểm tra password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Thành công: có thể trả về Token nếu dùng JWT, đây tạm thời trả về thông báo
        // thành công
        return ApiResponse.success("Login successful");
    }

    @Override
    public ApiResponse<String> forgotPassword(com.example.DienCamTamThe.dto.request.ForgotPasswordRequest request) {
        // Tìm user theo username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ApiResponse.success("Password reset successful");
    }
}
