package com.example.diencamtamthe.repository;

import com.example.diencamtamthe.model.So8GioSinhTamThe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface So8GioSinhTamTheRepository extends JpaRepository<So8GioSinhTamThe, Integer> {
    List<So8GioSinhTamThe> findByGioSinh(String gioSinh);
    List<So8GioSinhTamThe> findByGioSinhAndPhanGio(String gioSinh, String phanGio);
}
