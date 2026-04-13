package com.example.DienCamTamThe.service.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import com.example.DienCamTamThe.util.LunarCalendarUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.example.DienCamTamThe.service.strategy.DivinationStrategy;
import com.example.DienCamTamThe.service.strategy.DivinationStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DivinationServiceImpl {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DivinationStrategyFactory strategyFactory;

    // Định nghĩa 34 Sở (Sở 4 → Sở 37) đúng theo Database thực tế
    private static final int FIRST_SECTION = 4;
    private static final int LAST_SECTION = 32;

    private static final String[] SECTION_TITLES = {
            /* Sở 4 */ "Coi tuổi Mạng (Ngũ hành)",
            /* Sở 5 */ "Coi Tâm giờ sanh (Ngày đêm)",
            /* Sở 6 */ "Coi Tam Thế (Tháng/Giờ/Tuổi/Mạng)",
            /* Sở 7 */ "Coi Hồn đầu thai",
            /* Sở 8 */ "Coi 36 giờ sanh",
            /* Sở 9 */ "Coi Ngày sanh",
            /* Sở 10 */ "Coi Thọ thai sanh (Cung mệnh)",
            /* Sở 11 */ "Coi Nghề nghiệp",
            /* Sở 12 */ "Coi Cốt con gì",
            /* Sở 13 */ "Coi tuổi vợ con nít sanh",
            /* Sở 14 */ "Coi nuôi thú vật",
            /* Sở 15 */ "Coi điền viên (Ruộng đất)",
            /* Sở 16 */ "Coi học giỏi dở",
            /* Sở 17 */ "Coi thi cử (Kỳ nhất, kỳ nhì)",
            /* Sở 18 */ "Coi đi tù hay không",
            /* Sở 19 */ "Coi phá sản vợ chồng",
            /* Sở 20 */ "Coi duyên nợ vợ chồng",
            /* Sở 21 */ "Coi Vòng Trường Sinh",
            /* Sở 22 */ "Coi nuôi con",
            /* Sở 23 */ "Coi anh em",
            /* Sở 24 */ "Coi huynh đệ (Tài lộc)",
            /* Sở 25 */ "Coi con vua",
            /* Sở 26 */ "Coi con nhà",
            /* Sở 27 */ "Coi ngày kỵ hạp",
            /* Sở 28 */ "Coi sao chiếu mệnh",
            /* Sở 29 */ "Coi hành hạn năm",
            /* Sở 30 */ "Coi lời khuyên & nghi lễ",
            /* Sở 31 */ "Coi kết luận (Phần trăm)",
            /* Sở 32 */ "Coi khi chết"
    };

    public String processDivination(DivinationRequest request) {
        StringBuilder content = new StringBuilder();
        content.append("<div style='text-align: left;'>");

        String category = request.getLookupCategory();
        if (category == null)
            category = "Tất cả";

        // Lấy thông số đầu vào
        int rawYear = 1999;
        int rawDay = 1;
        int rawMonth = 1;
        try {
            rawYear = Integer.parseInt(request.getBirthYear());
            rawDay = Integer.parseInt(request.getBirthDay());
            rawMonth = Integer.parseInt(request.getBirthMonth());
        } catch (Exception e) {
        }

        // Chuyển đổi sang Âm lịch nếu là Dương lịch
        int birthYear, ngaySinh, thangSinh;
        String calendarNote = "";

        String calendarType = request.getCalendarType() != null ? request.getCalendarType().toLowerCase() : "";
        boolean isSolar = calendarType.contains("solar") || calendarType.contains("duong")
                || calendarType.contains("dương");

        if (isSolar) {
            LunarCalendarUtil.LunarDate lunar = LunarCalendarUtil.convertSolarToLunar(rawDay, rawMonth, rawYear, 7.0);
            birthYear = lunar.year;
            thangSinh = lunar.month;
            ngaySinh = lunar.day;
            calendarNote = String.format(" (Đã quy đổi từ Dương lịch %02d/%02d/%d)", rawDay, rawMonth, rawYear);
        } else {
            birthYear = rawYear;
            thangSinh = rawMonth;
            ngaySinh = rawDay;
        }

        String hr = (request.getBirthHour() == null || request.getBirthHour().isEmpty()) ? "00"
                : request.getBirthHour();
        String mi = (request.getBirthMinute() == null || request.getBirthMinute().isEmpty()) ? "00"
                : request.getBirthMinute();
        if (hr.length() == 1)
            hr = "0" + hr;
        if (mi.length() == 1)
            mi = "0" + mi;
        String gioSinhFull = hr + ":" + mi;

        String canChiGio = extractCanChi(gioSinhFull);
        int thangThoThai = thangSinh - 9;
        if (thangThoThai <= 0)
            thangThoThai += 12;

        String can = LunarCalendarUtil.getCan(birthYear);
        String chi = LunarCalendarUtil.getChi(birthYear);
        String cot = mapChiToCot(chi.toLowerCase());
        String mang = calculateMenh(getCanIndex(can.toLowerCase()), getChiIndex(chi.toLowerCase()));

        boolean foundAny = false;

        content.append(
                "<div class='log-box' style='background: #fff9eb; padding: 18px; border-radius: 12px; border: 1px solid #eec; margin-bottom: 25px; color: #333; box-shadow: 0 4px 15px rgba(0,0,0,0.05);'>");
        content.append("<strong style='color: #8c1010; font-size: 1.1em;'>[Thông số Diễn Cầm]</strong><br>");
        content.append("<div style='margin-left: 10px; margin-top: 8px; line-height: 1.6;'>");
        content.append("- Ngày sinh (Âm lịch): <b>").append(ngaySinh).append("/").append(thangSinh).append("/")
                .append(birthYear).append("</b>").append(calendarNote).append("<br>");
        content.append("- Bạn tuổi: <b>").append(can.toUpperCase()).append(" ").append(chi.toUpperCase())
                .append("</b><br>");
        content.append("- Mạng (Ngũ Hành): <b>").append(mapNguHanhToVietnamese(mang).toUpperCase()).append("</b><br>");
        content.append("<span style='color: #274e13;'>=> Đang tra cứu chuyên mục: <b>").append(category)
                .append("</b></span>");
        content.append("</div></div>");

        // Tính TruongSanhID 1 lần để dùng cho Sở 22, 23
        int truongSanhId = lookupTruongSanhId(getChiId(chi), getNguHanhId(mang));

        // Vòng lặp từ Sở 4 đến 37 để xử lý từng Sở
        for (int i = FIRST_SECTION; i <= LAST_SECTION; i++) {
            if (!isSectionInCategory(i, category))
                continue;

            try {
                foundAny = true;
                int titleIndex = i - FIRST_SECTION;
                String title = (titleIndex >= 0 && titleIndex < SECTION_TITLES.length) ? SECTION_TITLES[titleIndex]
                        : "Sở " + i;

                // Header for extraction (H4)
                content.append("\n<hr><h4>Sở ").append(i).append(". ").append(title).append("</h4>\n");
                // Detailed header for View All (Hidden by default)
                content.append(
                        "<div class='view-all-header' style='display: none; color: #8c1010; font-size: 1.3em; font-weight: bold; margin-top: 40px; border-bottom: 1px dashed #ccc; padding-bottom: 5px; text-align: center; font-family: serif; text-transform: uppercase;'>SỞ SỐ ")
                        .append(i).append("</div>");

                // Xử lý truy vấn động vào bảng soXX tương ứng
                processDynamicSection(content, i, request, can, chi, ngaySinh, thangSinh, canChiGio, thangThoThai, mang,
                        cot, truongSanhId, gioSinhFull);
            } catch (Exception e) {
                content.append("<p style='color:red'>Lỗi tải dữ liệu: ").append(e.getMessage()).append("</p>");
            }
        }

        if (!foundAny) {
            content.append("<p>Không có dữ liệu chi tiết cho hạng mục này.</p>");
        }

        content.append("</div>");
        return content.toString();
    }

    private boolean isSectionInCategory(int secNo, String filterCategory) {
        // Sections 33, 34, 35, 36, 37 are now removed or merged.
        // We skip processing them to comply with the request to make them disappear.
        if (secNo >= 33 && secNo <= 37) {
            return false;
        }

        if (filterCategory.equals("Tất cả")) {
            return true;
        }
        // Áp dụng lại theo số Sở thực tế (4-37)
        if (filterCategory.contains("Tình cảm")) {
            return secNo == 7 || secNo == 19 || secNo == 20 || secNo == 21 || secNo == 22 || secNo == 23;
        }
        if (filterCategory.contains("Công danh")) {
            return secNo == 11 || secNo == 13 || secNo == 16 || secNo == 17 || secNo == 29;
        }
        if (filterCategory.contains("Tài lộc")) {
            return secNo == 15 || secNo == 24 || secNo == 26;
        }
        if (filterCategory.contains("Sức khỏe")) {
            return secNo == 30 || secNo == 31 || secNo == 32 || (secNo >= 34 && secNo <= 37);
        }
        if (filterCategory.contains("Gia đình")) {
            return secNo == 13 || secNo == 23 || secNo == 22;
        }
        if (filterCategory.contains("Vận hạn")) {
            return secNo == 18 || secNo == 28 || secNo == 29;
        }
        if (filterCategory.contains("Bản thân")) {
            return secNo == 4 || secNo == 5 || secNo == 6 || secNo == 8 || secNo == 9 || secNo == 10 || secNo == 12
                    || secNo == 25 || secNo == 30 || secNo == 33 || secNo == 34 || secNo == 35 || secNo == 37;
        }
        return true;
    }

    private int lookupTruongSanhId(int chiId, int nguHanhId) {
        try {
            // Sở 21 lưu ở bảng so21_coi_truong_sanh với các cột Chi_ID, Ngu_Hanh_ID,
            // Truong_Sanh_ID
            String sql = "SELECT Truong_Sanh_ID FROM so21_coi_truong_sanh WHERE Chi_ID = ? AND Ngu_Hanh_ID = ? LIMIT 1";
            List<?> results = entityManager.createNativeQuery(sql)
                    .setParameter(1, chiId)
                    .setParameter(2, nguHanhId)
                    .getResultList();
            if (!results.isEmpty() && results.get(0) != null) {
                return Integer.parseInt(results.get(0).toString());
            }
        } catch (Exception e) {
            System.err.println("Lỗi lookup TruongSanhID: " + e.getMessage());
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    private void processDynamicSection(StringBuilder content, int secNo, DivinationRequest request,
            String can, String chi, int ngaySinh, int thangSinh,
            String canChiGio, int thangThoThai, String mang, String cot,
            int truongSanhId, String gioSinhFull) {

        String prefix1 = String.format("so%02d", secNo);

        List<String> tables = new ArrayList<>();
        try {
            if (secNo == 31) {
                // User explicit request: Sở 31 only uses so31_dinh_so_song_lau
                tables.add("so31_dinh_so_song_lau");
            } else if (secNo == 7) {
                // User explicit request: Sở 7 only uses so07_hon_dau_thai
                tables.add("so07_hon_dau_thai");
            } else {
                // Try multiple prefix formats to be extremely robust
                String[] prefixes = {
                        String.format("so%02d", secNo), // so10, so04
                        String.format("so%d", secNo), // so10, so4
                        String.format("so_%02d", secNo), // so_10, so_04
                        String.format("so_%d", secNo) // so_10, so_4
                };

                java.util.Set<String> uniqueTables = new java.util.LinkedHashSet<>();
                for (String p : prefixes) {
                    List<?> rows = entityManager.createNativeQuery("SHOW TABLES LIKE '" + p + "%'").getResultList();
                    for (Object row : rows) {
                        uniqueTables.add(row.toString());
                    }
                }
                tables.addAll(uniqueTables);
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy bảng cho Sở " + secNo + ": " + e.getMessage());
        }

        int canId = getCanId(can);
        int chiId = getChiId(chi);
        int mangId = getNguHanhId(mang);
        String mua = getMua(thangSinh);
        String buoi = getBuoi(gioSinhFull);

        if (tables.isEmpty()) {
            content.append("<p><em>(Nội dung cho Sở ").append(secNo)
                    .append(" đang được cập nhật trong Database...)</em></p>");
            return;
        }

        boolean foundContent = false;

        DivinationStrategy strategy = strategyFactory.getStrategy(secNo);
        if (strategy != null) {
            strategy.process(content, request, can, chi, ngaySinh, thangSinh, canChiGio, thangThoThai, mang, cot, truongSanhId, gioSinhFull);
            return;
        }
        
        // ===================== GENERIC DYNAMIC QUERY LOGIC =====================
        for (String table : tables) {
            try {
                List<Object[]> cols = entityManager.createNativeQuery("SHOW COLUMNS FROM `" + table + "`")
                        .getResultList();
                List<String> colNames = new java.util.ArrayList<>();
                List<String> colNamesOriginal = new java.util.ArrayList<>();
                for (Object[] colObj : cols) {
                    colNames.add(colObj[0].toString().toLowerCase());
                    colNamesOriginal.add(colObj[0].toString());
                }

                List<String> conditions = new java.util.ArrayList<>();
                List<Object> params = new java.util.ArrayList<>();

                String canCol = findColumn(colNames, "canid", "can_id", "tuoi_canid");
                if (canCol != null) {
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, canCol) + "` = ?");
                    params.add(canId);
                }

                String chiCol = findColumn(colNames, "chiid", "chi_id", "tuoi_chiid");
                if (chiCol != null) {
                    int chiVal = (secNo == 8 || secNo == 5) ? getChiId(canChiGio) : chiId;
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, chiCol) + "` = ?");
                    params.add(chiVal);
                }

                if (chiCol == null && colNames.contains("tuoi_nam")) {
                    conditions.add("`tuoi_nam` = ?");
                    params.add(chi);
                }

                String mangCol = findColumn(colNames, "nguhanhid", "ngu_hanhid", "mang_id");
                if (mangCol != null) {
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, mangCol) + "` = ?");
                    params.add(mangId);
                } else if (colNames.contains("mang")) {
                    conditions.add("(`Mang` = ? OR `Mang` = ?)");
                    params.add(mangId);
                    params.add(mapNguHanhToVietnamese(mang));
                }

                String thangCol = findColumn(colNames, "thangsanh", "thang_sanh", "sanh_thang", "thang_sinh",
                        "thang_ky", "thang");
                if (thangCol != null) {
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, thangCol) + "` = ?");
                    params.add(thangSinh);
                }

                String ngayCol = findColumn(colNames, "ngaysanh", "ngay");
                if (ngayCol != null) {
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, ngayCol) + "` = ?");
                    params.add(ngaySinh);
                }

                String gioCol = findColumn(colNames, "gioid", "gio_id", "gio_ky", "ten_gio_chi");
                if (gioCol != null) {
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, gioCol) + "` = ?");
                    if (gioCol.equals("gio_ky") || gioCol.equals("ten_gio_chi")) {
                        params.add(canChiGio);
                    } else {
                        params.add(getChiId(canChiGio));
                    }
                }

                if (colNames.contains("tho_thai_thang")) {
                    conditions.add("`tho_thai_thang` = ?");
                    params.add(thangThoThai);
                }

                if (colNames.contains("buoi")) {
                    conditions.add("`buoi` = ?");
                    params.add(buoi);
                }

                String muaCol = findColumn(colNames, "muaid", "mua_id", "mua");
                if (muaCol != null) {
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, muaCol) + "` = ?");
                    params.add(mua);
                }

                String genderReq = request.getGender();
                String genderCol = findColumn(colNames, "gioitinh", "gioi_tinh");
                if (genderReq != null && genderCol != null) {
                    String genderVn = getGenderVn(genderReq);
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, genderCol) + "` = ?");
                    params.add(genderVn);
                }

                String tuoiCol = findColumn(colNames, "tuoiamlich", "tuoi_am_lich");
                if (tuoiCol != null) {
                    int cYear = java.time.Year.now().getValue();
                    int bYear = 1999;
                    try {
                        bYear = Integer.parseInt(request.getBirthYear());
                    } catch (Exception e) {
                    }
                    int tuoiAmLich = cYear - bYear + 1;
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, tuoiCol) + "` = ?");
                    params.add(tuoiAmLich);
                }

                String tsCol = findColumn(colNames, "truongsanhid", "truong_sanhid");
                if (tsCol != null) {
                    if (truongSanhId != -1) {
                        conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, tsCol) + "` = ?");
                        params.add(truongSanhId);
                    } else {
                        continue;
                    }
                }

                if (conditions.isEmpty()) {
                    String tl = table.toLowerCase();
                    if (!(tl.contains("tongquan") || tl.contains("loikhuyen") || tl.contains("trietly")
                            || tl.contains("loi_ket") || tl.contains("nghile") || tl.contains("nhan_qua")
                            || tl.contains("ngu_tang") || tl.contains("nguphuong"))) {
                        continue;
                    }
                }

                String whereClause = conditions.isEmpty() ? "1=1" : String.join(" AND ", conditions);
                List<String> outputCols = new java.util.ArrayList<>();
                for (int ci = 0; ci < colNames.size(); ci++) {
                    if (isOutputColumn(colNames.get(ci)))
                        outputCols.add(colNamesOriginal.get(ci));
                }
                if (outputCols.isEmpty())
                    outputCols.add(colNamesOriginal.get(colNamesOriginal.size() - 1));

                String selectFields = "`" + String.join("`, `", outputCols) + "`";
                String sql = "SELECT " + selectFields + " FROM `" + table + "` WHERE " + whereClause + " LIMIT 20";

                try {
                    var query = entityManager.createNativeQuery(sql);
                    for (int pi = 0; pi < params.size(); pi++)
                        query.setParameter(pi + 1, params.get(pi));
                    List<?> rawResults = query.getResultList();

                    if (!rawResults.isEmpty()) {
                        foundContent = true;
                        if (tables.size() > 1) {
                            String cleanName = table.substring(table.indexOf('_') + 1).replace("_", " ");
                            content.append("<h6 style='color:#b8860b; margin-top:10px;'>▶ ")
                                    .append(cleanName.toUpperCase()).append("</h6>");
                        }
                        for (Object rawRow : rawResults) {
                            Object[] row = (rawRow instanceof Object[]) ? (Object[]) rawRow : new Object[] { rawRow };
                            StringBuilder rowVal = new StringBuilder();
                            for (int ri = 0; ri < row.length; ri++) {
                                if (row[ri] != null) {
                                    String val = row[ri].toString().replace("\n", "<br>");
                                    if (row.length > 1 && val.length() < 50 && ri < row.length - 1) {
                                        // Bỏ dấu ":" cho các Sở người dùng yêu cầu (14, 15, 16, 25, 26, 28, 29...)
                                        if (secNo == 14 || secNo == 15 || secNo == 16 || secNo == 25 || secNo == 26
                                                || secNo == 28 || secNo == 29) {
                                            rowVal.append("<strong>").append(val).append("</strong><br>");
                                        } else {
                                            rowVal.append("<strong>").append(val).append(":</strong> ");
                                        }
                                    } else {
                                        rowVal.append(val).append("<br>");
                                    }
                                }
                            }
                            if (rowVal.length() > 0) {
                                content.append("<p style='margin-bottom:8px;'>").append(rowVal.toString())
                                        .append("</p>");
                            }
                        }
                    }
                } catch (Exception eQuery) {
                    System.err.println("Lỗi query table " + table + ": " + eQuery.getMessage());
                }

            } catch (Exception e) {
                System.err.println("Lỗi query động bảng " + table + ": " + e.getMessage());
            }
        }

        if (!foundContent) {
            if (secNo == 18 || secNo == 19 || secNo == 20 || secNo == 27) {
                content.append(
                        "<p><em>(Chúc mừng bạn, theo sách Diễn Cầm Tam Thế, tuổi và tháng sinh của bạn không phạm phải tai ương này)</em></p>");
            } else {
                content.append(
                        "<p><em>(Sở này đã có bảng nhưng chưa có dữ liệu trùng khớp với thông tin của bạn)</em></p>");
            }
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Tìm cột đầu tiên khớp trong danh sách tên cột (đã lowercase).
     * Trả về tên cột lowercase nếu tìm thấy, null nếu không.
     */
    private String findColumn(List<String> colNames, String... candidates) {
        for (String candidate : candidates) {
            if (colNames.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Lấy tên cột gốc (chưa lowercase) từ danh sách.
     */
    private String getOriginalCol(List<String> originals, List<String> lowered, String loweredName) {
        int idx = lowered.indexOf(loweredName);
        return idx >= 0 ? originals.get(idx) : loweredName;
    }

    /**
     * Kiểm tra xem cột có phải là cột output (nội dung hiển thị) hay không.
     * Trả về true nếu cột nên hiển thị, false nếu là cột filter/ID.
     */
    private boolean isOutputColumn(String colLower) {
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

    private String getGenderVn(String genderReq) {
        if (genderReq == null)
            return "Nam";
        return (genderReq.toLowerCase().contains("fe") || genderReq.toLowerCase().contains("nu")
                || genderReq.toLowerCase().contains("nữ")) ? "Nữ" : "Nam";
    }

    private String getMua(int thangSinh) {
        if (thangSinh >= 1 && thangSinh <= 3)
            return "Xuân";
        if (thangSinh >= 4 && thangSinh <= 6)
            return "Hạ";
        if (thangSinh >= 7 && thangSinh <= 9)
            return "Thu";
        return "Đông";
    }

    private String getGiaiDoanGio(String gioSinhFull) {
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

    private String getBuoi(String gioSinh) {
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

    private int getCanId(String can) {
        String[] cans = { "giáp", "ất", "bính", "đinh", "mậu", "kỷ", "canh", "tân", "nhâm", "quý" };
        String lower = can.toLowerCase();
        for (int i = 0; i < cans.length; i++) {
            if (lower.contains(cans[i]))
                return i + 1;
        }
        return 1;
    }

    private int getChiId(String chi) {
        String[] chis = { "tý", "sửu", "dần", "mẹo", "thìn", "tỵ", "ngọ", "mùi", "thân", "dậu", "tuất", "hợi" };
        String lower = chi.toLowerCase();
        for (int i = 0; i < chis.length; i++) {
            if (lower.contains(chis[i]) || (lower.contains("mão") && i == 3))
                return i + 1;
        }
        return 1;
    }

    private int getNguHanhId(String mang) {
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

    private String mapNguHanhToVietnamese(String mang) {
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

    private String extractCanChi(String rawGio) {
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

    private String lookupBoneName(int chiId, int thangSinh) {
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

    private int findColIndex(List<String> columns, String... targets) {
        for (String target : targets) {
            int idx = columns.indexOf(target.toLowerCase());
            if (idx >= 0)
                return idx;
        }
        return -1;
    }

    private String mapChiToCot(String chi) {
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

    private int getCanIndex(String can) {
        String[] cans = { "canh", "tân", "nhâm", "quý", "giáp", "ất", "bính", "đinh", "mậu", "kỷ" };
        String[] cansAscii = { "canh", "tan", "nham", "quy", "giap", "at", "binh", "dinh", "mau", "ky" };
        for (int i = 0; i < cans.length; i++) {
            if (cans[i].equalsIgnoreCase(can) || cansAscii[i].equalsIgnoreCase(can))
                return i;
        }
        return 0;
    }

    private int getChiIndex(String chi) {
        String[] chis = { "thân", "dậu", "tuất", "hợi", "tý", "sửu", "dần", "mẹo", "thìn", "tỵ", "ngọ", "mùi" };
        String[] chisAscii = { "than", "dau", "tuat", "hoi", "ty1", "suu", "dan", "mao", "thin", "ty2", "ngo", "mui" };
        for (int i = 0; i < chis.length; i++) {
            if (chis[i].equalsIgnoreCase(chi) || chisAscii[i].equalsIgnoreCase(chi))
                return i;
        }
        return 0;
    }

    private String getCanName(int canId) {
        String[] cans = { "", "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý" };
        if (canId >= 1 && canId <= 10)
            return cans[canId];
        return String.valueOf(canId);
    }

    private String getChiName(int chiId) {
        String[] chis = { "", "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi" };
        if (chiId >= 1 && chiId <= 12)
            return chis[chiId];
        return "";
    }

    private int calculateDayChiId(int d, int m, int y) {
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

    private List<String> getTableColumnNames(String tableName) {
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

    private void appendExaminationResult(StringBuilder content, String tableName, int chiId, int thangSinh) {
        // ... không cần thiết vì đã dùng inline logic ở trên, nhưng có thể giữ nếu muốn
        // refactor
    }

    private void appendSo13TableResult(StringBuilder content, String tableName, String idColName, Object idValue,
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

    private String calculateMenh(int canIndex, int chiIndex) {
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

}