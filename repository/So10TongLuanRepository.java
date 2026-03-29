package com.example.diencamtamthe.repository;

import com.example.diencamtamthe.model.So10TongLuan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface So10TongLuanRepository extends JpaRepository<So10TongLuan, Integer> {
    List<So10TongLuan> findByThangThoThaiAndThangSinh(Integer thangThoThai, Integer thangSinh);
}
