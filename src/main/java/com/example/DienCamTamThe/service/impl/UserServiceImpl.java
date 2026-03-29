package com.example.DienCamTamThe.service.impl;

import com.example.DienCamTamThe.dto.request.RegisterRequest;
import com.example.DienCamTamThe.dto.response.ApiResponse;
import com.example.DienCamTamThe.entity.User;
import com.example.DienCamTamThe.exception.UserAlreadyExistsException;
import com.example.DienCamTamThe.repository.UserRepository;
import com.example.DienCamTamThe.service.UserService;
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
}
