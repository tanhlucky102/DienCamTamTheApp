package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import com.example.DienCamTamThe.service.strategy.DivinationStrategy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

public abstract class AbstractDivinationStrategy implements DivinationStrategy {

    @PersistenceContext
    protected EntityManager entityManager;

protected String findColumn(List<String> colNames, String... candidates) {
        for (String candidate : candidates) {
            if (colNames.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

protected String getOriginalCol(List<String> originals, List<String> lowered, String loweredName) {
        int idx = lowered.indexOf(loweredName);
        return idx >= 0 ? originals.get(idx) : loweredName;
    }

protected boolean isOutputColumn(String colLower) {
        // Loại bỏ các cột ID / filter
        if (colLower.equals("id"))
            return false;
        if (colLower.equals("so") || colLower.equals("so_muc") || colLower.equals("somuc"))
            return false;
        if (colLower.equals("created_at"))
            return false;

        // Loại các cột dùng để filter (ID columns)
        if (colLower.equals("canid") || colLower.equals("can_id") || colLower.equals("tuoi_canid"))
            return false;
        if (colLower.equals("chiid") || colLower.equals("chi_id") || colLower.equals("tuoi_chiid"))
            return false;
        if (colLower.equals("nguhanhid") || colLower.equals("ngu_hanhid") || colLower.equals("mang_id"))
            return false;
        if (colLower.equals("truongsanhid") || colLower.equals("truong_sanhid"))
            return false;
        if (colLower.equals("muaid") || colLower.equals("mua_id"))
            return false;
        if (colLower.equals("gioid") || colLower.equals("gio_id") || colLower.equals("ten_gio_chi"))
            return false;

        // Loại các cột đầu vào filter
        if (colLower.equals("thangsanh") || colLower.equals("thang_sanh") || colLower.equals("sanh_thang"))
            return false;
        if (colLower.equals("thang_sinh") || colLower.equals("thang_ky") || colLower.equals("thang"))
            return false;
        if (colLower.equals("ngaysanh") || colLower.equals("ngay"))
            return false;
        if (colLower.equals("gio_ky"))
            return false;
        if (colLower.equals("gioitinh") || colLower.equals("gioi_tinh"))
            return false;
        if (colLower.equals("tuoiamlich") || colLower.equals("tuoi_am_lich"))
            return false;
        if (colLower.equals("tuoi_nam"))
            return false;
        if (colLower.equals("buoi"))
            return false;
        if (colLower.equals("mua"))
            return false;
        if (colLower.equals("tho_thai_thang"))
            return false;

        return true;
    }

protected String getGenderVn(String genderReq) {
        if (genderReq == null)
            return "Nam";
        return (genderReq.toLowerCase().contains("fe") || genderReq.toLowerCase().contains("nu")
                || genderReq.toLowerCase().contains("nữ")) ? "Nữ" : "Nam";
    }

protected String getMua(int thangSinh) {
        if (thangSinh >= 1 && thangSinh <= 3)
            return "Xuân";
        if (thangSinh >= 4 && thangSinh <= 6)
            return "Hạ";
        if (thangSinh >= 7 && thangSinh <= 9)
            return "Thu";
        return "Đông";
    }

protected String getGiaiDoanGio(String gioSinhFull) {
        if (gioSinhFull == null || !gioSinhFull.contains(":"))
            return "Giữa";
        try {
            String[] parts = gioSinhFull.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);

            // Tìm giờ bắt đầu của khung 2 tiếng (23, 1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21)
            int hStart = (h % 2 == 0) ? h - 1 : h;
            if (h == 0)
                hStart = 23;
            else if (hStart < 0)
                hStart = 23;

            int totalMinutesOffset;
            if (h == 0 && hStart == 23) {
                totalMinutesOffset = 60 + m;
            } else if (h == 23 && hStart == 23) {
                totalMinutesOffset = m;
            } else {
                totalMinutesOffset = (h - hStart) * 60 + m;
            }

            if (totalMinutesOffset < 40)
                return "Đầu";
            if (totalMinutesOffset < 80)
                return "Giữa";
            return "Sau";
        } catch (Exception e) {
            return "Giữa";
        }
    }

protected String getBuoi(String gioSinh) {
        if (gioSinh == null || gioSinh.isEmpty())
            return "Ngay";
        try {
            // Trích xuất giờ từ chuỗi input
            String hourStr = gioSinh.trim();
            if (hourStr.contains(":")) {
                hourStr = hourStr.substring(0, hourStr.indexOf(":")).trim();
            }
            int hour = Integer.parseInt(hourStr);
            // Đêm: 17h-04h (Dậu, Tuất, Hợi, Tý, Sửu, Dần)
            if (hour >= 17 || hour <= 4)
                return "Dem";
        } catch (Exception e) {
        }
        return "Ngay";
    }

protected int getCanId(String can) {
        String[] cans = { "giáp", "ất", "bính", "đinh", "mậu", "kỷ", "canh", "tân", "nhâm", "quý" };
        String lower = can.toLowerCase();
        for (int i = 0; i < cans.length; i++) {
            if (lower.contains(cans[i]))
                return i + 1;
        }
        return 1;
    }

protected int getChiId(String chi) {
        String[] chis = { "tý", "sửu", "dần", "mẹo", "thìn", "tỵ", "ngọ", "mùi", "thân", "dậu", "tuất", "hợi" };
        String lower = chi.toLowerCase();
        for (int i = 0; i < chis.length; i++) {
            if (lower.contains(chis[i]) || (lower.contains("mão") && i == 3))
                return i + 1;
        }
        return 1;
    }

protected int getNguHanhId(String mang) {
        String lower = mang.toLowerCase();
        if (lower.contains("kim"))
            return 1;
        if (lower.contains("mộc") || lower.contains("moc"))
            return 2;
        if (lower.contains("thủy") || lower.contains("thuy"))
            return 3;
        if (lower.contains("hỏa") || lower.contains("hoa"))
            return 4;
        if (lower.contains("thổ") || lower.contains("tho"))
            return 5;
        return 1;
    }

protected String mapNguHanhToVietnamese(String mang) {
        String lower = mang.toLowerCase();
        if (lower.contains("kim"))
            return "Kim";
        if (lower.contains("thuy") || lower.contains("thủy"))
            return "Thủy";
        if (lower.contains("hoa") || lower.contains("hỏa"))
            return "Hỏa";
        if (lower.contains("tho") || lower.contains("thổ"))
            return "Thổ";
        if (lower.contains("moc") || lower.contains("mộc"))
            return "Mộc";
        return mang;
    }

protected String extractCanChi(String rawGio) {
        if (rawGio == null || rawGio.isEmpty())
            return "Ngọ";
        if (rawGio.contains(":") && rawGio.length() >= 5) {
            try {
                int h = Integer.parseInt(rawGio.substring(0, 2).trim());
                if (h >= 23 || h < 1)
                    return "Tý";
                if (h >= 1 && h < 3)
                    return "Sửu";
                if (h >= 3 && h < 5)
                    return "Dần";
                if (h >= 5 && h < 7)
                    return "Mẹo";
                if (h >= 7 && h < 9)
                    return "Thìn";
                if (h >= 9 && h < 11)
                    return "Tỵ";
                if (h >= 11 && h < 13)
                    return "Ngọ";
                if (h >= 13 && h < 15)
                    return "Mùi";
                if (h >= 15 && h < 17)
                    return "Thân";
                if (h >= 17 && h < 19)
                    return "Dậu";
                if (h >= 19 && h < 21)
                    return "Tuất";
                if (h >= 21 && h < 23)
                    return "Hợi";
            } catch (Exception e) {
            }
        }
        // Fallback: parse từ chuỗi Chi trực tiếp
        int idx = rawGio.indexOf(" ");
        return idx > 0 ? rawGio.substring(0, idx).trim() : rawGio;
    }

protected String lookupBoneName(int chiId, int thangSinh) {
        try {
            // Check multiple table names for robustness
            String[] tables = { "so12_coi_cot_con_gi", "so12_cot_con_gi", "so12_tuoi_thang_cot" };
            for (String table : tables) {
                try {
                    // Try to find columns dynamically since it might vary
                    List<Object[]> cols = entityManager.createNativeQuery("SHOW COLUMNS FROM `" + table + "`")
                            .getResultList();
                    List<String> colNames = new java.util.ArrayList<>();
                    for (Object[] col : cols)
                        colNames.add(col[0].toString().toLowerCase());

                    int chiColIdx = findColIndex(colNames, "chiid", "tuoi_chiid", "chi", "chi_id");
                    int tsColIdx = findColIndex(colNames, "thangsanh", "thang_sanh", "thang");
                    int cotColIdx = findColIndex(colNames, "tencot", "ten_cot", "cot", "ketqua");

                    if (chiColIdx >= 0 && tsColIdx >= 0 && cotColIdx >= 0) {
                        String sql = "SELECT `" + colNames.get(cotColIdx) + "` FROM `" + table + "` WHERE `" +
                                colNames.get(chiColIdx) + "` = ? AND `" + colNames.get(tsColIdx) + "` = ? LIMIT 1";
                        List<?> results = entityManager.createNativeQuery(sql)
                                .setParameter(1, chiId)
                                .setParameter(2, thangSinh)
                                .getResultList();
                        if (!results.isEmpty() && results.get(0) != null) {
                            return results.get(0).toString().trim();
                        }
                    }
                } catch (Exception inner) {
                    // Ignore and try next table
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi lookupBoneName: " + e.getMessage());
        }
        // Fallback to static mapping if DB lookup fails
        return mapChiToCot(getChiName(chiId));
    }

protected int findColIndex(List<String> columns, String... targets) {
        for (String target : targets) {
            int idx = columns.indexOf(target.toLowerCase());
            if (idx >= 0)
                return idx;
        }
        return -1;
    }

protected String mapChiToCot(String chi) {
        if (chi == null)
            return "chuot";
        String lower = chi.toLowerCase();
        if (lower.contains("tý") || lower.contains("ty1"))
            return "chuot";
        if (lower.contains("sửu") || lower.contains("suu"))
            return "trau";
        if (lower.contains("dần") || lower.contains("dan"))
            return "cop";
        if (lower.contains("mẹo") || lower.contains("mão") || lower.contains("mao"))
            return "tho";
        if (lower.contains("thìn") || lower.contains("thin"))
            return "rong";
        if (lower.contains("tỵ") || lower.contains("ty2"))
            return "ran";
        if (lower.contains("ngọ") || lower.contains("ngo"))
            return "ngua";
        if (lower.contains("mùi") || lower.contains("mui"))
            return "de";
        if (lower.contains("thân") || lower.contains("than"))
            return "khi";
        if (lower.contains("dậu") || lower.contains("dau"))
            return "ga";
        if (lower.contains("tuất") || lower.contains("tuat"))
            return "cho";
        if (lower.contains("hợi") || lower.contains("hoi"))
            return "heo";
        return "chuot";
    }

protected int getCanIndex(String can) {
        String[] cans = { "canh", "tân", "nhâm", "quý", "giáp", "ất", "bính", "đinh", "mậu", "kỷ" };
        String[] cansAscii = { "canh", "tan", "nham", "quy", "giap", "at", "binh", "dinh", "mau", "ky" };
        for (int i = 0; i < cans.length; i++) {
            if (cans[i].equalsIgnoreCase(can) || cansAscii[i].equalsIgnoreCase(can))
                return i;
        }
        return 0;
    }

protected int getChiIndex(String chi) {
        String[] chis = { "thân", "dậu", "tuất", "hợi", "tý", "sửu", "dần", "mẹo", "thìn", "tỵ", "ngọ", "mùi" };
        String[] chisAscii = { "than", "dau", "tuat", "hoi", "ty1", "suu", "dan", "mao", "thin", "ty2", "ngo", "mui" };
        for (int i = 0; i < chis.length; i++) {
            if (chis[i].equalsIgnoreCase(chi) || chisAscii[i].equalsIgnoreCase(chi))
                return i;
        }
        return 0;
    }

protected String getCanName(int canId) {
        String[] cans = { "", "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý" };
        if (canId >= 1 && canId <= 10)
            return cans[canId];
        return String.valueOf(canId);
    }

protected String getChiName(int chiId) {
        String[] chis = { "", "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi" };
        if (chiId >= 1 && chiId <= 12)
            return chis[chiId];
        return "";
    }

protected int calculateDayChiId(int d, int m, int y) {
        if (m < 3) {
            y--;
            m += 12;
        }
        // Công thức Julian Day đơn giản hóa để tính Chi ngày của Việt Nam
        long jd = (long) (365.25 * (y + 4716)) + (long) (30.6001 * (m + 1)) + d - 1524;
        int chi = (int) ((jd + 1) % 12);
        if (chi < 0)
            chi += 12;
        return chi + 1; // 1=Tý, ..., 12=Hợi
    }

protected void appendExaminationResult(StringBuilder content, String tableName, int chiId, int thangSinh) {
        // ... không cần thiết vì đã dùng inline logic ở trên, nhưng có thể giữ nếu muốn
        // refactor
    }

protected void appendSo13TableResult(StringBuilder content, String tableName, String idColName, Object idValue,
            int thangSinh) {
        try {
            List<String> tablesFound = entityManager.createNativeQuery("SHOW TABLES LIKE '" + tableName + "'")
                    .getResultList();
            if (tablesFound.isEmpty())
                return;

            List<String> colNames = getTableColumnNames(tableName);
            if (colNames.isEmpty())
                return;
            List<String> lowerCols = colNames.stream().map(String::toLowerCase)
                    .collect(java.util.stream.Collectors.toList());

            int valColIdx = findColIndex(lowerCols, idColName.toLowerCase(), "canid", "chiid");
            int tsColIdx = findColIndex(lowerCols, "thangsanh", "thang_sanh", "thang");
            int resColIdx = findColIndex(lowerCols, "tenketqua", "tensao", "ten_ket_qua", "ten_sao", "ketqua", "sao");
            int lgColIdx = findColIndex(lowerCols, "loigiai", "loi_giai", "noi_dung", "mota");

            String sql = "SELECT * FROM `" + tableName + "` WHERE `" + colNames.get(valColIdx >= 0 ? valColIdx : 0)
                    + "` = ? AND `" + colNames.get(tsColIdx >= 0 ? tsColIdx : 1) + "` = ?";
            var query = entityManager.createNativeQuery(sql);
            query.setParameter(1, idValue);
            query.setParameter(2, thangSinh);
            List<?> rows = query.getResultList();

            for (Object objRow : rows) {
                Object[] row = (Object[]) objRow;
                String resName = (resColIdx >= 0 && row[resColIdx] != null) ? row[resColIdx].toString() : "";
                String loiGiaiVal = (lgColIdx >= 0 && row[lgColIdx] != null)
                        ? row[lgColIdx].toString().replace("\n", "<br>")
                        : "N/A";

                content.append("<div style='margin-bottom:8px;'>• <b>").append(resName).append("</b>: ")
                        .append(loiGiaiVal).append("</div>");
            }
        } catch (Exception e) {
            System.err.println("Lỗi lookup bảng " + tableName + ": " + e.getMessage());
        }
    }

protected String calculateMenh(int canIndex, int chiIndex) {
        int canVal = 0;
        if (canIndex == 4 || canIndex == 5)
            canVal = 1;
        else if (canIndex == 6 || canIndex == 7)
            canVal = 2;
        else if (canIndex == 8 || canIndex == 9)
            canVal = 3;
        else if (canIndex == 0 || canIndex == 1)
            canVal = 4;
        else if (canIndex == 2 || canIndex == 3)
            canVal = 5;

        int chiVal = 0;
        if (chiIndex == 4 || chiIndex == 5 || chiIndex == 10 || chiIndex == 11)
            chiVal = 0;
        else if (chiIndex == 6 || chiIndex == 7 || chiIndex == 0 || chiIndex == 1)
            chiVal = 1;
        else
            chiVal = 2;

        int finalMenh = canVal + chiVal;
        if (finalMenh > 5)
            finalMenh -= 5;

        switch (finalMenh) {
            case 1:
                return "Kim";
            case 2:
                return "Thủy";
            case 3:
                return "Hỏa";
            case 4:
                return "Thổ";
            case 5:
                return "Mộc";
            default:
                return "Kim";
        }
    }

protected List<String> getTableColumnNames(String tableName) {
        try {
            List<Object[]> colsInfo = entityManager.createNativeQuery("SHOW COLUMNS FROM `" + tableName + "`")
                    .getResultList();
            List<String> colNames = new java.util.ArrayList<>();
            for (Object[] col : colsInfo) {
                colNames.add(col[0].toString());
            }
            return colNames;
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
}
