package com.example.diencamtamthe.repository;

import com.example.diencamtamthe.model.BookEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookEntryRepository extends JpaRepository<BookEntry, String> {
    List<BookEntry> findBySectionCode(String sectionCode);
}
