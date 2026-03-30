package com.example.DienCamTamThe.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "book_entries")
public class BookEntry {

    @Id
    @Column(name = "entry_id", length = 50)
    private String entryId;

    @Column(name = "section_code", length = 50)
    private String sectionCode;

    @Column(name = "input_data", columnDefinition = "LONGTEXT")
    private String inputData;

    @Column(name = "output_data", columnDefinition = "LONGTEXT")
    private String outputData;

    public String getEntryId() { return entryId; }
    public void setEntryId(String entryId) { this.entryId = entryId; }
    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }
    public String getInputData() { return inputData; }
    public void setInputData(String inputData) { this.inputData = inputData; }
    public String getOutputData() { return outputData; }
    public void setOutputData(String outputData) { this.outputData = outputData; }
}
