package com.example.DienCamTamThe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.DienCamTamThe.model.User;
import com.example.DienCamTamThe.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String register(User user) {

        // check trùng username
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return "Username already exists";
        }

        // mã hóa password
        user.setPassword(encoder.encode(user.getPassword()));

        userRepository.save(user);

        return "Register success";
    }
}