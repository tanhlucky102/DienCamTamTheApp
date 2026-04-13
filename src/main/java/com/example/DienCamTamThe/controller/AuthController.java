package com.example.DienCamTamThe.controller;

import com.example.DienCamTamThe.dto.request.*;
import com.example.DienCamTamThe.dto.response.ApiResponse;
import com.example.DienCamTamThe.dto.response.UserInfoResponse;
import com.example.DienCamTamThe.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String SESSION_KEY = "loggedInUser";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /** ĐĂNG KÝ */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserInfoResponse>> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    /** ĐĂNG NHẬP – lưu username vào HttpSession */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserInfoResponse>> login(@RequestBody LoginRequest request,
                                                               HttpSession session) {
        ApiResponse<UserInfoResponse> resp = userService.login(request);
        if (resp.getStatus() == 200 && resp.getData() != null) {
            session.setAttribute(SESSION_KEY, resp.getData().getUsername());
        }
        return ResponseEntity.ok(resp);
    }

    /** ĐĂNG XUẤT – huỷ session */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công."));
    }

    /** LẤY SESSION HIỆN TẠI – frontend gọi để biết ai đang đăng nhập */
    @GetMapping("/session")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getSession(HttpSession session) {
        String username = (String) session.getAttribute(SESSION_KEY);
        if (username == null) {
            return ResponseEntity.ok(ApiResponse.error(401, "Chưa đăng nhập."));
        }
        return ResponseEntity.ok(userService.getUserInfo(username));
    }

    /** QUÊN MẬT KHẨU */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(userService.forgotPassword(request));
    }

    /** LẤY THÔNG TIN HỒ SƠ (dùng session, không cần truyền username) */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMe(HttpSession session) {
        String username = (String) session.getAttribute(SESSION_KEY);
        if (username == null) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "Chưa đăng nhập."));
        }
        return ResponseEntity.ok(userService.getUserInfo(username));
    }

    /** CẬP NHẬT HỒ SƠ (dùng session) */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateProfile(
            @RequestBody UpdateProfileRequest request,
            HttpSession session) {
        String username = (String) session.getAttribute(SESSION_KEY);
        if (username == null) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "Chưa đăng nhập."));
        }
        return ResponseEntity.ok(userService.updateProfile(username, request));
    }

    /** ĐỔI MẬT KHẨU (dùng session lấy username) */
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestBody ChangePasswordRequest request,
            HttpSession session) {
        String username = (String) session.getAttribute(SESSION_KEY);
        if (username == null) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "Chưa đăng nhập."));
        }
        request.setUsername(username); // lấy từ session, không cần frontend gửi
        return ResponseEntity.ok(userService.changePassword(request));
    }
}