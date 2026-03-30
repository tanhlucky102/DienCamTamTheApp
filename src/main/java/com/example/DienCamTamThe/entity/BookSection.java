package com.example.DienCamTamThe.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "book_sections")
public class BookSection {

    @Id
    @Column(name = "section_code", length = 50)
    private String sectionCode;

    @Column(name = "section_no")
    private Integer sectionNo;

    @Column(name = "title")
    private String title;

    @Column(name = "category", length = 50)
    private String category;

    // Getters and setters
    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }
    public Integer getSectionNo() { return sectionNo; }
    public void setSectionNo(Integer sectionNo) { this.sectionNo = sectionNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
