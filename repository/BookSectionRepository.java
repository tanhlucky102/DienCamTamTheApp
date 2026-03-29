package com.example.diencamtamthe.repository;

import com.example.diencamtamthe.model.BookSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookSectionRepository extends JpaRepository<BookSection, String> {
    List<BookSection> findAllByOrderBySectionNoAsc();
}
