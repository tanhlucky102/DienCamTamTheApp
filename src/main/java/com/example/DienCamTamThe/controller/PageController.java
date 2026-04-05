package com.example.DienCamTamThe.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private static final String SESSION_KEY = "loggedInUser";

    @GetMapping("/auth")
    public String authPage(HttpSession session) {
        // Nếu đã đăng nhập rồi → redirect thẳng vào dashboard
        if (session.getAttribute(SESSION_KEY) != null) {
            return "redirect:/dashboard";
        }
        return "auth";
    }

    @GetMapping("/dashboard")
    public String dashboardPage(HttpSession session) {
        if (session.getAttribute(SESSION_KEY) == null) {
            return "redirect:/auth";
        }
        return "dashboard";
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session) {
        if (session.getAttribute(SESSION_KEY) == null) {
            return "redirect:/auth";
        }
        return "profile";
    }

    @GetMapping("/history")
    public String historyPage(HttpSession session) {
        if (session.getAttribute(SESSION_KEY) == null) {
            return "redirect:/auth";
        }
        return "history";
    }
}
