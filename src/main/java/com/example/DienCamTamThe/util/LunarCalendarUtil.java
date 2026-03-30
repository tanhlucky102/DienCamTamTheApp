package com.example.DienCamTamThe.util;

/**
 * Tiện ích chuyển đổi ngày Dương lịch sang Âm lịch (Việt Nam)
 * Dựa trên thuật toán của Hồ Ngọc Đức.
 */
public class LunarCalendarUtil {

    public static class LunarDate {
        public int day;
        public int month;
        public int year;
        public boolean isLeap;

        public LunarDate(int day, int month, int year, boolean isLeap) {
            this.day = day;
            this.month = month;
            this.year = year;
            this.isLeap = isLeap;
        }

        @Override
        public String toString() {
            return String.format("%02d/%02d/%d%s", day, month, year, isLeap ? " (Nhuận)" : "");
        }
    }

    private static double jdFromDate(int d, int m, int y) {
        double a = Math.floor((14 - m) / 12.0);
        y = y + 4800 - (int) a;
        m = m + 12 * (int) a - 3;
        double jd = d + Math.floor((153 * m + 2) / 5.0) + 365 * y + Math.floor(y / 4.0) - Math.floor(y / 100.0) + Math.floor(y / 400.0) - 32045;
        if (jd < 2299161) {
            jd = d + Math.floor((153 * m + 2) / 5.0) + 365 * y + Math.floor(y / 4.0) - 32083;
        }
        return jd;
    }

    private static int getNewMoon(int k, double timeZone) {
        double t = k / 1236.85;
        double t2 = t * t;
        double t3 = t2 * t;
        double dr = Math.PI / 180;
        double j1 = 2451550.09765 + 29.530588853 * k + 0.0001337 * t2 - 0.00000015 * t3 + 0.00000000073 * t * t3;
        double m = (2.5534 + 29.10535669 * k - 0.00000218 * t2 - 0.00000011 * t3) * dr;
        double mprime = (201.5643 + 385.81693528 * k + 0.0107438 * t2 + 0.00001239 * t3 - 0.00000005 * t * t3) * dr;
        double f = (160.7108 + 390.67050274 * k - 0.0016341 * t2 - 0.00000011 * t3 + 0.00000001 * t * t3) * dr;
        // e is not used in the simplified result summation
        double deltaJ = (0.1734 - 0.000393 * t) * Math.sin(m)
                + 0.0021 * Math.sin(2 * m)
                - 0.4068 * Math.sin(mprime)
                + 0.0161 * Math.sin(2 * mprime)
                - 0.0004 * Math.sin(3 * mprime)
                + 0.0104 * Math.sin(2 * f)
                - 0.0051 * Math.sin(m + mprime)
                - 0.0074 * Math.sin(m - mprime)
                + 0.0004 * Math.sin(2 * f + m)
                - 0.0004 * Math.sin(2 * f - m)
                - 0.0006 * Math.sin(2 * f + mprime)
                + 0.0100 * Math.sin(2 * f - mprime)
                + 0.0005 * Math.sin(m + 2 * mprime);
        return (int) (j1 + deltaJ + timeZone / 24.0 + 0.5);
    }

    public static LunarDate convertSolarToLunar(int d, int m, int y, double timeZone) {
        int jd = (int) jdFromDate(d, m, y);
        int k = (int) Math.floor((jd - 2451550.1) / 29.530588853);
        int nm = getNewMoon(k, timeZone);
        if (nm > jd) {
            k--;
            nm = getNewMoon(k, timeZone);
        }
        
        // Lấy tháng 11 gần nhất trước đó
        int k11 = (int) Math.floor((jd - 2451545) / 29.530588853 / 12) * 12 + 10;
        int nm11 = getNewMoon(k11, timeZone);
        if (nm11 > jd) {
            k11 -= 12;
            nm11 = getNewMoon(k11, timeZone);
        } else {
            if (getNewMoon(k11 + 12, timeZone) <= jd) {
                k11 += 12;
                nm11 = getNewMoon(k11, timeZone);
            }
        }
        
        int lunarDay = jd - nm + 1;
        int monthsSince11 = k - k11;
        int lunarMonth = (11 + monthsSince11) % 12;
        if (lunarMonth == 0) lunarMonth = 12;
        
        int lunarYear = y;
        if (lunarMonth >= 11) {
            int tetK = k11 + 2;
            int nmTet = getNewMoon(tetK, timeZone);
            if (jd < nmTet) {
                lunarYear = y - 1;
            }
        } else {
            lunarYear = y;
            if (m == 1) {
                lunarYear = y - 1;
            } else if (m == 2 && d < 20) {
                // Check edge case near Tet
                int k_jan = (int) Math.floor((jdFromDate(1, 1, y) - 2451550) / 29.53);
                int tetK = k_jan + 1; 
                int nmTet = getNewMoon(tetK, timeZone);
                if (jd < nmTet) lunarYear = y - 1;
            }
        }
        
        return new LunarDate(lunarDay, lunarMonth, lunarYear, false);
    }

    public static String getCan(int year) {
        String[] cans = {"Canh", "Tân", "Nhâm", "Quý", "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ"};
        int index = (year % 10);
        if (index < 0) index += 10;
        return cans[index];
    }

    public static String getChi(int year) {
        String[] chis = {"Thân", "Dậu", "Tuất", "Hợi", "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi"};
        int index = (year % 12);
        if (index < 0) index += 12;
        return chis[index];
    }
}
