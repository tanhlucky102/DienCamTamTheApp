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

    private static double INT(double d) {
        return Math.floor(d);
    }

    private static double jdFromDate(int dd, int mm, int yy) {
        double a, y, m, jd;
        a = INT((14 - mm) / 12.0);
        y = yy + 4800 - a;
        m = mm + 12 * a - 3;
        jd = dd + INT((153 * m + 2) / 5.0) + 365 * y + INT(y / 4.0) - INT(y / 100.0) + INT(y / 400.0) - 32045;
        if (jd < 2299161) {
            jd = dd + INT((153 * m + 2) / 5.0) + 365 * y + INT(y / 4.0) - 32083;
        }
        return jd;
    }

    private static double NewMoon(int k) {
        double T = k / 1236.85; 
        double T2 = T * T;
        double T3 = T2 * T;
        double dr = Math.PI / 180;
        double Jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * T2 - 0.000000155 * T3;
        Jd1 = Jd1 + 0.00033 * Math.sin((166.56 + 132.87 * T - 0.009173 * T2) * dr);
        double M = 359.2242 + 29.10535608 * k - 0.0000333 * T2 - 0.00000347 * T3;
        double Mpr = 306.0253 + 385.81691806 * k + 0.0107306 * T2 + 0.00001236 * T3;
        double F = 21.2964 + 390.67050646 * k - 0.0016528 * T2 - 0.00000239 * T3;
        double C1 = (0.1734 - 0.000393 * T) * Math.sin(M * dr) + 0.0021 * Math.sin(2 * dr * M);
        C1 = C1 - 0.4068 * Math.sin(Mpr * dr) + 0.0161 * Math.sin(dr * 2 * Mpr);
        C1 = C1 - 0.0004 * Math.sin(dr * 3 * Mpr);
        C1 = C1 + 0.0104 * Math.sin(dr * 2 * F) - 0.0051 * Math.sin(dr * (M + Mpr));
        C1 = C1 - 0.0074 * Math.sin(dr * (M - Mpr)) + 0.0004 * Math.sin(dr * (2 * F + M));
        C1 = C1 - 0.0004 * Math.sin(dr * (2 * F - M)) - 0.0006 * Math.sin(dr * (2 * F + Mpr));
        C1 = C1 + 0.0010 * Math.sin(dr * (2 * F - Mpr)) + 0.0005 * Math.sin(dr * (2 * Mpr + M));
        double deltat;
        if (T < -11) {
            deltat = 0.001 + 0.000839 * T + 0.0002261 * T2 - 0.00000845 * T3 - 0.000000081 * T * T3;
        } else {
            deltat = -0.000278 + 0.000265 * T + 0.000262 * T2;
        }
        return Jd1 + C1 - deltat;
    }

    private static double SunLongitude(double jdn) {
        double T = (jdn - 2451545.0) / 36525.0;
        double T2 = T * T;
        double dr = Math.PI / 180;
        double M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2;
        double L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2;
        double DL = (1.914600 - 0.004817 * T - 0.000014 * T2) * Math.sin(dr * M);
        DL = DL + (0.019993 - 0.000101 * T) * Math.sin(dr * 2 * M) + 0.000290 * Math.sin(dr * 3 * M);
        double L = L0 + DL;
        L = L * dr;
        L = L - Math.PI * 2 * (INT(L / (Math.PI * 2)));
        return L;
    }

    private static int getSunLongitude(double dayNumber, double timeZone) {
        return (int) INT(SunLongitude(dayNumber - 0.5 - timeZone / 24.0) / Math.PI * 6.0);
    }

    private static double getNewMoonDay(int k, double timeZone) {
        return INT(NewMoon(k) + 0.5 + timeZone / 24.0);
    }

    private static double getLunarMonth11(int yy, double timeZone) {
        double off = jdFromDate(31, 12, yy) - 2415021;
        int k = (int) INT(off / 29.530588853);
        double nm = getNewMoonDay(k, timeZone);
        int sunLong = getSunLongitude(nm, timeZone);
        if (sunLong >= 9) {
            nm = getNewMoonDay(k - 1, timeZone);
        }
        return nm;
    }

    private static int getLeapMonthOffset(double a11, double timeZone) {
        int k = (int) INT((a11 - 2415021.076998695) / 29.530588853 + 0.5);
        int last = 0;
        int i = 1;
        int arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone);
        do {
            last = arc;
            i++;
            arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone);
        } while (arc != last && i < 14);
        return i - 1;
    }

    public static LunarDate convertSolarToLunar(int dd, int mm, int yy, double timeZone) {
        int k;
        double dayNumber, monthStart, a11, b11;
        int lunarDay, lunarMonth, lunarYear, lunarLeap, diff, leapMonthDiff;
        
        dayNumber = jdFromDate(dd, mm, yy);
        k = (int) INT((dayNumber - 2415021.076998695) / 29.530588853);
        monthStart = getNewMoonDay(k + 1, timeZone);
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k, timeZone);
        }
        
        a11 = getLunarMonth11(yy, timeZone);
        b11 = a11;
        
        if (a11 >= monthStart) {
            lunarYear = yy;
            a11 = getLunarMonth11(yy - 1, timeZone);
        } else {
            lunarYear = yy + 1;
            b11 = getLunarMonth11(yy + 1, timeZone);
        }
        
        lunarDay = (int) (dayNumber - monthStart + 1);
        diff = (int) INT((monthStart - a11) / 29.0);
        lunarLeap = 0;
        lunarMonth = diff + 11;
        
        if (b11 - a11 > 365) {
            leapMonthDiff = getLeapMonthOffset(a11, timeZone);
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10;
                if (diff == leapMonthDiff) {
                    lunarLeap = 1;
                }
            }
        }
        
        if (lunarMonth > 12) {
            lunarMonth = lunarMonth - 12;
        }
        if (lunarMonth >= 11 && diff < 4) {
            lunarYear -= 1;
        }
        
        return new LunarDate(lunarDay, lunarMonth, lunarYear, lunarLeap != 0);
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

    public static String getCanChiString(int year) {
        return getCan(year) + " " + getChi(year);
    }

    // --- BẢNG DỮ LIỆU NẠP ÂM VÀ CÂN XƯƠNG ---
    private static final String[] NAP_AM_TABLE = {
        "Hải Trung Kim", "Hải Trung Kim", "Lư Trung Hỏa", "Lư Trung Hỏa", "Đại Lâm Mộc", "Đại Lâm Mộc", 
        "Lộ Bàng Thổ", "Lộ Bàng Thổ", "Kiếm Phong Kim", "Kiếm Phong Kim", "Sơn Đầu Hỏa", "Sơn Đầu Hỏa", 
        "Giản Hạ Thủy", "Giản Hạ Thủy", "Thành Đầu Thổ", "Thành Đầu Thổ", "Bạch Lạp Kim", "Bạch Lạp Kim", 
        "Dương Liễu Mộc", "Dương Liễu Mộc", "Tuyền Trung Thủy", "Tuyền Trung Thủy", "Ốc Thượng Thổ", "Ốc Thượng Thổ", 
        "Thích Lịch Hỏa", "Thích Lịch Hỏa", "Tùng Bách Mộc", "Tùng Bách Mộc", "Trường Lưu Thủy", "Trường Lưu Thủy", 
        "Sa Trung Kim", "Sa Trung Kim", "Sơn Hạ Hỏa", "Sơn Hạ Hỏa", "Bình Địa Mộc", "Bình Địa Mộc", 
        "Bích Thượng Thổ", "Bích Thượng Thổ", "Kim Bạch Kim", "Kim Bạch Kim", "Phúc Đăng Hỏa", "Phúc Đăng Hỏa", 
        "Thiên Hà Thủy", "Thiên Hà Thủy", "Đại Trạch Thổ", "Đại Trạch Thổ", "Thoa Xuyến Kim", "Thoa Xuyến Kim", 
        "Tang Đố Mộc", "Tang Đố Mộc", "Đại Khê Thủy", "Đại Khê Thủy", "Sa Trung Thổ", "Sa Trung Thổ", 
        "Thiên Thượng Hỏa", "Thiên Thượng Hỏa", "Thạch Lựu Mộc", "Thạch Lựu Mộc", "Đại Hải Thủy", "Đại Hải Thủy"
    };

    private static final int[] WEIGHT_YEAR = {
        12, 6, 6, 7, 12, 5, 9, 8, 7, 8, 15, 9, 16, 8, 8, 19, 12, 6, 8, 7, 5, 15, 6, 16, 12, 7, 8, 12, 10, 7, 
        15, 6, 5, 14, 14, 9, 7, 7, 9, 12, 8, 7, 13, 5, 14, 5, 9, 17, 5, 7, 12, 8, 8, 6, 19, 6, 8, 16, 10, 6
    };

    private static final int[] WEIGHT_MONTH = {
        6, 7, 18, 9, 5, 16, 9, 15, 18, 8, 9, 5
    };

    private static final int[] WEIGHT_DAY = {
        5, 10, 8, 15, 16, 15, 8, 16, 8, 16, 9, 17, 8, 17, 10, 8, 9, 18, 5, 15, 10, 9, 8, 9, 15, 18, 7, 8, 16, 6
    };

    private static final int[] WEIGHT_HOUR = {
        16, 6, 7, 10, 9, 16, 10, 8, 8, 9, 6, 6
    };

    public static int getCanChiIndexYear(int lunarYear) {
        int val = (lunarYear - 4) % 60;
        if (val < 0) val += 60;
        return val;
    }

    public static String getNapAm(int lunarYear) {
        return NAP_AM_TABLE[getCanChiIndexYear(lunarYear)];
    }

    public static int getChiIndexHour(String gioSinhStr) {
        String hour = gioSinhStr != null ? gioSinhStr.toLowerCase().trim() : "";
        if (hour.startsWith("tý") || hour.startsWith("ty1") || hour.startsWith("chuột")) return 0;
        if (hour.startsWith("sửu") || hour.startsWith("trâu")) return 1;
        if (hour.startsWith("dần") || hour.startsWith("hổ") || hour.startsWith("cọp")) return 2;
        if (hour.startsWith("mão") || hour.startsWith("mẹo") || hour.startsWith("thỏ")) return 3;
        if (hour.startsWith("thìn") || hour.startsWith("rồng")) return 4;
        if (hour.startsWith("tỵ") || hour.startsWith("ty2") || hour.startsWith("rắn")) return 5;
        if (hour.startsWith("ngọ") || hour.startsWith("ngựa")) return 6;
        if (hour.startsWith("mùi") || hour.startsWith("dê")) return 7;
        if (hour.startsWith("thân") || hour.startsWith("khỉ")) return 8;
        if (hour.startsWith("dậu") || hour.startsWith("gà")) return 9;
        if (hour.startsWith("tuất") || hour.startsWith("chó")) return 10;
        if (hour.startsWith("hợi") || hour.startsWith("heo") || hour.startsWith("lợn")) return 11;
        return 6; // Mặc định là Ngọ
    }

    public static String getCanXuong(int lunarYear, int lunarMonth, int lunarDay, String hourStr) {
        int wY = WEIGHT_YEAR[getCanChiIndexYear(lunarYear)];
        
        int safeMonth = Math.max(1, Math.min(12, lunarMonth));
        int wM = WEIGHT_MONTH[safeMonth - 1]; 
        
        int safeDay = Math.max(1, Math.min(30, lunarDay));
        int wD = WEIGHT_DAY[safeDay - 1]; 
        
        int wH = WEIGHT_HOUR[getChiIndexHour(hourStr)]; 

        int totalChi = wY + wM + wD + wH;
        int luong = totalChi / 10;
        int chi = totalChi % 10;

        return luong + " lượng " + chi + " chỉ";
    }
}
