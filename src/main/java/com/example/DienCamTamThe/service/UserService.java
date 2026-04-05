package com.example.DienCamTamThe.service;

import com.example.DienCamTamThe.dto.request.*;
import com.example.DienCamTamThe.dto.response.ApiResponse;
import com.example.DienCamTamThe.dto.response.UserInfoResponse;

public interface UserService {
    ApiResponse<UserInfoResponse> register(RegisterRequest request);

    ApiResponse<UserInfoResponse> login(LoginRequest request);

    ApiResponse<String> forgotPassword(ForgotPasswordRequest request);

    ApiResponse<UserInfoResponse> getUserInfo(String username);

    ApiResponse<UserInfoResponse> updateProfile(String username, UpdateProfileRequest request);

    ApiResponse<String> changePassword(ChangePasswordRequest request);
}