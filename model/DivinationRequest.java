package com.example.diencamtamthe.model;

public class DivinationRequest {
    private String fullname;
    private String gender;
    private String calendarType;
    private String birthHour;
    private String birthMinute;
    private String birthDay;
    private String birthMonth;
    private String birthYear;
    private String lookupCategory;

    public DivinationRequest() {
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCalendarType() {
        return calendarType;
    }

    public void setCalendarType(String calendarType) {
        this.calendarType = calendarType;
    }

    public String getBirthHour() {
        return birthHour;
    }

    public void setBirthHour(String birthHour) {
        this.birthHour = birthHour;
    }

    public String getBirthMinute() {
        return birthMinute;
    }

    public void setBirthMinute(String birthMinute) {
        this.birthMinute = birthMinute;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public String getBirthMonth() {
        return birthMonth;
    }

    public void setBirthMonth(String birthMonth) {
        this.birthMonth = birthMonth;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    public String getLookupCategory() {
        return lookupCategory;
    }

    public void setLookupCategory(String lookupCategory) {
        this.lookupCategory = lookupCategory;
    }
}
