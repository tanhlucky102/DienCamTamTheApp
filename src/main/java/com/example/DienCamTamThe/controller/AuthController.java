package com.example.DienCamTamThe.controller;

import com.example.DienCamTamThe.dto.request.LoginRequest;
import com.example.DienCamTamThe.dto.request.RegisterRequest;
import com.example.DienCamTamThe.dto.response.ApiResponse;
import com.example.DienCamTamThe.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }
}