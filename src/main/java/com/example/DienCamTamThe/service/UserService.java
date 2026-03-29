package com.example.DienCamTamThe.service;

import com.example.DienCamTamThe.dto.request.LoginRequest;
import com.example.DienCamTamThe.dto.request.RegisterRequest;
import com.example.DienCamTamThe.dto.response.ApiResponse;

public interface UserService {
    ApiResponse<String> register(RegisterRequest request);
    ApiResponse<String> login(LoginRequest request);
}