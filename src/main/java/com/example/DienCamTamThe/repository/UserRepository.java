package com.example.DienCamTamThe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.DienCamTamThe.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}