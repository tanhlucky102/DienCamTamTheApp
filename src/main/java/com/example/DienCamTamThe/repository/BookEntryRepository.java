package com.example.DienCamTamThe.repository;

import com.example.DienCamTamThe.entity.BookEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookEntryRepository extends JpaRepository<BookEntry, String> {
    List<BookEntry> findBySectionCode(String sectionCode);
}
