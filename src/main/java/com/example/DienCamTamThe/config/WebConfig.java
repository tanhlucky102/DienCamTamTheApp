package com.example.DienCamTamThe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Tự động chuyển hướng từ '/' sang trang '/auth.html' cho tiện lợi
        registry.addRedirectViewController("/", "/auth.html");
    }
}
