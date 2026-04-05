package com.example.DienCamTamThe.service.impl;

import com.example.DienCamTamThe.dto.request.*;
import com.example.DienCamTamThe.dto.response.ApiResponse;
import com.example.DienCamTamThe.dto.response.UserInfoResponse;
import com.example.DienCamTamThe.entity.User;
import com.example.DienCamTamThe.exception.UserAlreadyExistsException;
import com.example.DienCamTamThe.repository.UserRepository;
import com.example.DienCamTamThe.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    // =========================================
    // Helper: Convert User entity → DTO
    // =========================================
    private UserInfoResponse toResponse(User user) {
        return new UserInfoResponse(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getPhone(),
            user.getDateOfBirth()
        );
    }

    // =========================================
    // ĐĂNG KÝ
    // =========================================
    @Override
    public ApiResponse<UserInfoResponse> register(RegisterRequest request) {
        // Dùng email làm username nếu username rỗng
        String usernameToUse = (request.getUsername() != null && !request.getUsername().isBlank())
                ? request.getUsername()
                : request.getEmail();

        if (userRepository.existsByUsername(usernameToUse)) {
            throw new UserAlreadyExistsException("Tài khoản '" + usernameToUse + "' đã tồn tại.");
        }

        User user = new User();
        user.setUsername(usernameToUse);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail() != null ? request.getEmail() : usernameToUse);

        userRepository.save(user);

        return ApiResponse.success(toResponse(user));
    }

    // =========================================
    // ĐĂNG NHẬP
    // =========================================
    @Override
    public ApiResponse<UserInfoResponse> login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Tài khoản hoặc mật khẩu không chính xác."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Tài khoản hoặc mật khẩu không chính xác.");
        }

        return ApiResponse.success(toResponse(user));
    }

    // =========================================
    // QUÊN MẬT KHẨU (reset không cần mật khẩu cũ)
    // =========================================
    @Override
    public ApiResponse<String> forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản."));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ApiResponse.success("Cập nhật mật khẩu thành công.");
    }

    // =========================================
    // LẤY THÔNG TIN USER
    // =========================================
    @Override
    public ApiResponse<UserInfoResponse> getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản."));

        return ApiResponse.success(toResponse(user));
    }

    // =========================================
    // CẬP NHẬT HỒ SƠ
    // =========================================
    @Override
    public ApiResponse<UserInfoResponse> updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản."));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getEmail() != null)    user.setEmail(request.getEmail());
        if (request.getPhone() != null)    user.setPhone(request.getPhone());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());

        userRepository.save(user);

        return ApiResponse.success(toResponse(user));
    }

    // =========================================
    // ĐỔI MẬT KHẨU (cần xác minh mật khẩu cũ)
    // =========================================
    @Override
    public ApiResponse<String> changePassword(ChangePasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Mật khẩu hiện tại không đúng.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ApiResponse.success("Đổi mật khẩu thành công.");
    }
}
