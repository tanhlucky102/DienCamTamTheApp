package com.example.diencamtamthe.repository;

import com.example.diencamtamthe.model.So9LoiDoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface So9LoiDoanRepository extends JpaRepository<So9LoiDoan, Integer> {
    List<So9LoiDoan> findByTenHieu(String tenHieu);
}
