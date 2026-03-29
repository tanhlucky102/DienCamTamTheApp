package com.example.diencamtamthe.repository;

import com.example.diencamtamthe.model.So9HieuNgay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface So9HieuNgayRepository extends JpaRepository<So9HieuNgay, Integer> {
    // We will fetch all and find the matching day because it's a comma separated string
    // Real implementation might use fulltext or LIKE but we can process in java for safety
}
