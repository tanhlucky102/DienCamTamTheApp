package com.example.DienCamTamThe.util;

/**
 * Tiện ích chuyển đổi ngày Dương lịch sang Âm lịch (Việt Nam, GMT+7).
 * Thuật toán chuẩn của Hồ Ngọc Đức (https://www.informatik.uni-leipzig.de/~duc/amlich/).
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

    /** Số Julian Day từ ngày Dương lịch (lịch Gregory). */
    private static double jdFromDate(int dd, int mm, int yy) {
        int a = (int) Math.floor((14 - mm) / 12.0);
        int y = yy + 4800 - a;
        int m = mm + 12 * a - 3;
        double jd = dd + Math.floor((153.0 * m + 2) / 5)
                + 365L * y
                + Math.floor(y / 4.0)
                - Math.floor(y / 100.0)
                + Math.floor(y / 400.0)
                - 32045;
        return jd;
    }

    /**
     * Tính Julian Day đầu tháng (Sóc) thứ k kể từ epoch 1/1/2000 12:00 TT
     * @param k  chỉ số tháng (có thể âm)
     * @param timeZone múi giờ (7.0 cho VN)
     */
    private static int getNewMoon(int k, double timeZone) {
        double t  = k / 1236.85;
        double t2 = t * t;
        double t3 = t2 * t;
        double dr = Math.PI / 180;

        double jd1 = 2451550.09765
                   + 29.530588853 * k
                   + 0.0001337 * t2
                   - 0.00000015 * t3
                   + 0.00000000073 * t2 * t2;

        double m1     = (2.5534 + 29.10535669 * k - 0.0000218 * t2 - 0.00000011 * t3) * dr;
        double mPrime = (201.5643 + 385.81693528 * k + 0.0107438 * t2
                       + 0.00001239 * t3 - 0.000000058 * t2 * t2) * dr;
        double f      = (160.7108 + 390.67050274 * k - 0.0016341 * t2
                       - 0.00000227 * t3 + 0.000000011 * t2 * t2) * dr;
        double om     = (124.7746 - 1.5637558 * k + 0.0020691 * t2 + 0.00000215 * t3) * dr;
        double e      = 1 - 0.002516 * t - 0.0000074 * t2;

        double corr = (0.1734 - 0.000393 * t) * Math.sin(m1)
                    + 0.0021 * Math.sin(2 * m1)
                    - 0.4068 * Math.sin(mPrime)
                    + 0.0161 * Math.sin(2 * mPrime)
                    - 0.0004 * Math.sin(3 * mPrime)
                    + 0.0104 * Math.sin(2 * f)
                    - 0.0051 * e * Math.sin(m1 + mPrime)
                    - 0.0074 * e * Math.sin(m1 - mPrime)
                    + 0.0004 * Math.sin(2 * f + m1)
                    - 0.0004 * Math.sin(2 * f - m1)
                    - 0.0006 * Math.sin(2 * f + mPrime)
                    + 0.0100 * Math.sin(2 * f - mPrime)
                    + 0.0005 * Math.sin(m1 + 2 * mPrime)
                    + 0.0028 * e * Math.sin(2 * m1 + mPrime)
                    - 0.0009 * Math.sin(2 * m1 - mPrime)
                    - 0.0002 * e * Math.sin(3 * m1 + mPrime)
                    - 0.0004 * Math.sin(2 * mPrime - 2 * f)
                    - 0.0004 * Math.sin(2 * f - 2 * m1)
                    - 0.0006 * e * Math.sin(2 * f + m1 - mPrime)
                    + 0.0010 * e * Math.sin(2 * f - m1 + mPrime)
                    + 0.0005 * e * Math.sin(2 * m1 - mPrime + 2 * f)
                    + 0.0003 * e * Math.sin(m1 + mPrime - 2 * f)
                    - 0.0002 * Math.sin(om);

        return (int) Math.floor(jd1 + corr + 0.5 + timeZone / 24.0);
    }

    /**
     * Kiểm tra xem năm âm lịch lunarYear có tháng nhuận sau tháng 11 hay không.
     * Trả về true nếu trong vòng 12 tháng kể từ sóc tháng 11 năm đó có <= 12 tháng.
     */
    private static boolean isLeapYear(int k11, double timeZone) {
        int nm11  = getNewMoon(k11, timeZone);
        int nm12  = getNewMoon(k11 + 12, timeZone);
        // Nếu tổng số ngày từ nm11 đến nm12 vượt 365 → có tháng nhuận
        return nm12 - nm11 >= 366;
    }

    /**
     * Chuyển đổi Dương lịch → Âm lịch theo chuẩn Việt Nam (GMT+7).
     */
    public static LunarDate convertSolarToLunar(int dd, int mm, int yy, double timeZone) {
        int jd = (int) jdFromDate(dd, mm, yy);

        // k của ngày hôm nay
        int k = (int) Math.floor((jd - 2451550.1) / 29.530588853);

        // Sóc của tháng hiện tại
        int nm = getNewMoon(k, timeZone);
        if (nm > jd) {
            k--;
            nm = getNewMoon(k, timeZone);
        }

        // Tìm sóc tháng 11 âm lịch gần nhất trước hoặc chứa ngày jd
        // (tháng 11 là tháng chứa Đông Chí)
        int k11 = findK11(jd, yy, timeZone);

        boolean leap = isLeapYear(k11, timeZone);
        int monthsSince11 = k - k11;

        // Xác định tháng nhuận nếu có
        int leapMonth = -1;
        if (leap) {
            // Quét 12 tháng kể từ k11 để tìm tháng nhuận
            for (int i = 0; i < 14; i++) {
                int nm_i    = getNewMoon(k11 + i, timeZone);
                int nm_i1   = getNewMoon(k11 + i + 1, timeZone);
                boolean hasNoMajorSolarTerm = !hasMajorSolarTerm(nm_i, nm_i1, timeZone);
                if (hasNoMajorSolarTerm) {
                    leapMonth = i; // tháng thứ i tính từ k11 là tháng nhuận
                    break;
                }
            }
        }

        // Tính số tháng âm lịch
        int lunarMonth;
        boolean isLeapMonth = false;
        if (leap && leapMonth >= 0 && monthsSince11 >= leapMonth) {
            if (monthsSince11 == leapMonth) {
                // Chính xác rơi vào tháng nhuận
                isLeapMonth = true;
                lunarMonth = getBaseMonth(leapMonth, k11, timeZone);
            } else {
                lunarMonth = getBaseMonth(monthsSince11 - 1, k11, timeZone);
            }
        } else {
            lunarMonth = getBaseMonth(monthsSince11, k11, timeZone);
        }

        // Năm Âm lịch
        int lunarYear = yy;
        // Nếu tháng >= 11 và đang trước Tết → vẫn thuộc năm trước
        if (lunarMonth >= 11) {
            // Tìm sóc tháng 1 (Tết) năm yy
            int nmTet = getNewMoon(k11 + 2, timeZone); // k11+2 là tháng 1 AL
            if (jd < nmTet) {
                lunarYear = yy - 1;
            }
        }

        int lunarDay = jd - nm + 1;
        return new LunarDate(lunarDay, lunarMonth, lunarYear, isLeapMonth);
    }

    /** Tìm k11: chỉ số sóc tháng 11 âm lịch gần nhất không vượt qua jd */
    private static int findK11(int jd, int yy, double timeZone) {
        // Đông Chí năm yy rơi khoảng 21/12
        int jdDec = (int) jdFromDate(31, 12, yy);
        int k = (int) Math.floor((jdDec - 2451550.1) / 29.530588853);
        // Lùi về tháng chứa Đông Chí ~ tháng 11 AL
        int nm = getNewMoon(k, timeZone);

        // Đông Chí: khoảng Solar longitude = 270°
        // Đơn giản hơn: tháng 11 AL chứa khoảng 21/12 dương
        int jdWinter = (int) jdFromDate(21, 12, yy);
        while (nm > jdWinter) {
            k--;
            nm = getNewMoon(k, timeZone);
        }
        while (getNewMoon(k + 1, timeZone) <= jdWinter) {
            k++;
            nm = getNewMoon(k, timeZone);
        }
        // k bây giờ là k11 của năm AL tương ứng yy
        // Nhưng nếu jd < sóc tháng 11 này → dùng k11 của năm trước
        int k11 = k;
        if (nm > jd) {
            // Lùi 12 tháng để lấy k11 năm trước
            k11 -= 12;
        }
        return k11;
    }

    /** Kiểm tra khoảng [nm1, nm2) có chứa Middle Solar Term (Trung khí) không. */
    private static boolean hasMajorSolarTerm(int nm1, int nm2, double timeZone) {
        // Trung khí rơi vào khoảng mỗi 30.44 ngày, bắt đầu từ Xuân Phân JD ~2451623.8
        // Đơn giản: kiểm tra xem số ngày chứa bội số của 30.436875 kể từ điểm chuẩn
        double epoch = 2451589.2; // Xuân Phân 2000 approx
        int term1 = (int) Math.floor((nm1 - epoch) / 30.436875);
        int term2 = (int) Math.floor((nm2 - 1 - epoch) / 30.436875);
        return term1 < term2 || term1 == term2;
    }

    /** Lấy số tháng Âm lịch (1-12) từ chỉ số monthsSince11 */
    private static int getBaseMonth(int monthsSince11, int k11, double timeZone) {
        // monthsSince11 = 0 → tháng 11, 1 → tháng 12, 2 → tháng 1, ...
        int raw = (11 + monthsSince11) % 12;
        if (raw == 0) return 12;
        return raw;
    }

    // =========================================================================
    // Can / Chi từ năm Âm lịch
    // =========================================================================
    public static String getCan(int year) {
        // Giáp=1864, Ất=1865, ...; index = year % 10; 0→Canh...
        String[] cans = {"Canh", "Tân", "Nhâm", "Quý", "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ"};
        int index = year % 10;
        if (index < 0) index += 10;
        return cans[index];
    }

    public static String getChi(int year) {
        String[] chis = {"Thân", "Dậu", "Tuất", "Hợi", "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi"};
        int index = year % 12;
        if (index < 0) index += 12;
        return chis[index];
    }
}
