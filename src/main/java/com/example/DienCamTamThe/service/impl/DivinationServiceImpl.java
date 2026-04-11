package com.example.DienCamTamThe.service.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import com.example.DienCamTamThe.util.LunarCalendarUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DivinationServiceImpl {

    @PersistenceContext
    private EntityManager entityManager;

    // Định nghĩa 34 Sở (Sở 4 → Sở 37) đúng theo Database thực tế
    private static final int FIRST_SECTION = 4;
    private static final int LAST_SECTION = 37;

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
            /* Sở 32 */ "Coi khi chết",
            /* Sở 33 */ "Coi tổng luận (Lục tự)",
            /* Sở 34 */ "Coi ngũ phương & triết lý",
            /* Sở 35 */ "Coi nhân quả",
            /* Sở 36 */ "Coi ngũ tạng",
            /* Sở 37 */ "Lời kết"
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

        String hr = (request.getBirthHour() == null || request.getBirthHour().isEmpty()) ? "00" : request.getBirthHour();
        String mi = (request.getBirthMinute() == null || request.getBirthMinute().isEmpty()) ? "00" : request.getBirthMinute();
        if (hr.length() == 1) hr = "0" + hr;
        if (mi.length() == 1) mi = "0" + mi;
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

        // Hiển thị TỔNG QUAN trước (không phải từ DB, mà là metadata nhập liệu)
        content.append("\n<hr><h4>Sở Tổng Quan. Tam Thế Diễn Cầm TỔNG QUAN</h4>\n");
        content.append(
                "<div class='log-box' style='background: #fdf6e3; padding: 15px; border-radius: 8px; border: 1px solid #eee8d5; margin-bottom: 20px; color: #657b83'>");
        content.append("<strong style='color: #b58900;'>[Thông số Diễn Cầm]</strong><br>");
        content.append("- Ngày sinh (Âm lịch): <b>").append(ngaySinh).append("/").append(thangSinh).append("/")
                .append(birthYear).append("</b>").append(calendarNote).append("<br>");
        content.append("- Bạn tuổi: <b>").append(can.toUpperCase()).append(" ").append(chi.toUpperCase())
                .append("</b><br>");
        content.append("- Mạng (Ngũ Hành): <b>").append(mang.toUpperCase()).append("</b> - Cốt (Xương): <b>")
                .append(cot.toUpperCase()).append("</b><br>");
        content.append("<em>=> Đang tra cứu chuyên mục: <b>").append(category).append("</b></em>");
        content.append("</div>");

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
                content.append("\n<hr><h4>Sở ").append(i).append(". ").append(title).append("</h4>\n");

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

        // === SỞ 4: Coi tuổi Mạng (Ngũ hành) ===
        if (secNo == 4) {
            try {
                String sql = "SELECT t.NguHanhID, t.ChiTietMang, n.Name FROM so04_tuoimang t " +
                        "LEFT JOIN nguhanh n ON t.NguHanhID = n.ID " +
                        "WHERE t.CanID = ? AND t.ChiID = ? LIMIT 1";
                List<?> results = entityManager.createNativeQuery(sql)
                        .setParameter(1, canId).setParameter(2, chiId)
                        .getResultList();

                if (!results.isEmpty()) {
                    Object[] r = (Object[]) results.get(0);
                    String nguHanhName = r[2] != null ? r[2].toString() : "N/A";
                    String chiTiet = r[1] != null ? r[1].toString() : "";
                    content.append("<p><strong>Mạng ").append(nguHanhName).append(":</strong> ").append(chiTiet)
                            .append("</p>");
                    foundContent = true;
                }
            } catch (Exception e) {
                System.err.println("Lỗi Sở 4: " + e.getMessage());
            }
            if (foundContent)
                return;
        }

        // === SỞ 5: Tâm giờ sanh ===
        if (secNo == 5) {
            try {
                // Chuẩn hóa giờ sinh sang định dạng HH:mm:00 để so sánh chuỗi
                String searchTime = gioSinhFull + ":00";

                String sql = "SELECT tu_gio, den_gio, ten_gio_chi, buoi FROM so05_tamgiosanh WHERE thang_sanh = ?";
                List<?> results = entityManager.createNativeQuery(sql)
                        .setParameter(1, thangSinh)
                        .getResultList();

                content.append(
                        "<p>Dựa trên tháng sinh âm lịch và giờ sinh, chúng tôi xác định cung giờ chính xác của bạn:</p>");
                for (Object rawRow : results) {
                    Object[] r = (Object[]) rawRow;
                    String tuGio = r[0].toString();
                    String denGio = r[1].toString();
                    String tenGioChi = r[2].toString();
                    String buoiVn = r[3].toString();

                    boolean match = false;
                    if (tuGio.compareTo(denGio) <= 0) {
                        // Khoảng trong ngày (VD: 04:00 - 06:00)
                        if (searchTime.compareTo(tuGio) >= 0 && searchTime.compareTo(denGio) <= 0)
                            match = true;
                    } else {
                        // Khoảng qua đêm (VD: 22:00 - 02:00)
                        if (searchTime.compareTo(tuGio) >= 0 || searchTime.compareTo(denGio) <= 0)
                            match = true;
                    }

                    if (match) {
                        content.append("<p><strong>Kết quả:</strong> Bạn sanh vào <b>Giờ ").append(tenGioChi)
                                .append("</b>");
                        content.append(" (Khoảng ").append(tuGio.substring(0, 5)).append(" - ")
                                .append(denGio.substring(0, 5));
                        content.append(" thuộc buổi ").append(buoiVn.equals("Ngay") ? "Ngày" : "Đêm").append(").</p>");
                        foundContent = true;
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi Sở 5: " + e.getMessage());
            }
            if (foundContent)
                return;
        }

        // === SỞ 6: Coi Tam Thế (Tháng/Giờ/Tuổi/Mạng) ===
        if (secNo == 6) {
            try {
                // 1. Mạng mới sanh
                String sql1 = "SELECT n.Name, m.LoiGiai FROM so06_moi_sanh_mang_gi m JOIN nguhanh n ON m.NguHanhID = n.ID WHERE m.NguHanhID = ? LIMIT 1";
                List<?> res1 = entityManager.createNativeQuery(sql1).setParameter(1, mangId).getResultList();
                if (!res1.isEmpty()) {
                    Object[] r = (Object[]) res1.get(0);
                    content.append("<p><strong>Mạng ").append(r[0]).append(":</strong> ").append(r[1]).append("</p>");
                    foundContent = true;
                }

                int gioId = getChiId(canChiGio);
                java.util.Map<String, String> khMatches = new java.util.LinkedHashMap<>();
                java.util.Set<String> allNoiDunSet = new java.util.LinkedHashSet<>();

                // Tập hợp tất cả các NoiDung có thể có trong Sở 6
                String[] allKHTables = {
                        "so06_kiet_hung_can_gio", "so06_kiet_hung_mang_thang",
                        "so06_kiet_hung_nam_gio", "so06_kiet_hung_thang_gio",
                        "so06_kiet_hung_tuoi_thang"
                };

                for (String table : allKHTables) {
                    try {
                        List<?> allND = entityManager.createNativeQuery("SELECT DISTINCT NoiDung FROM " + table)
                                .getResultList();
                        for (Object nd : allND)
                            if (nd != null)
                                allNoiDunSet.add(nd.toString());
                    } catch (Exception e) {
                    }
                }

                // Thực hiện các query thực tế để tìm match
                try {
                    // Can + Giờ
                    List<?> res2 = entityManager
                            .createNativeQuery(
                                    "SELECT NoiDung, LoiGiai FROM so06_kiet_hung_can_gio WHERE CanID = ? AND GioID = ?")
                            .setParameter(1, canId).setParameter(2, gioId).getResultList();
                    for (Object obj : res2) {
                        Object[] r = (Object[]) obj;
                        khMatches.put(r[0].toString(), r[1].toString());
                    }

                    // Mạng + Tháng
                    List<?> res3 = entityManager.createNativeQuery(
                            "SELECT NoiDung, LoiGiai FROM so06_kiet_hung_mang_thang WHERE Mang = ? AND Thang = ?")
                            .setParameter(1, mangId).setParameter(2, thangSinh).getResultList();
                    for (Object obj : res3) {
                        Object[] r = (Object[]) obj;
                        khMatches.put(r[0].toString(), r[1].toString());
                    }

                    // Năm + Giờ
                    List<?> res4 = entityManager
                            .createNativeQuery(
                                    "SELECT NoiDung, LoiGiai FROM so06_kiet_hung_nam_gio WHERE ChiID = ? AND GioID = ?")
                            .setParameter(1, chiId).setParameter(2, gioId).getResultList();
                    for (Object obj : res4) {
                        Object[] r = (Object[]) obj;
                        khMatches.put(r[0].toString(), r[1].toString());
                    }

                    // Tháng + Giờ
                    List<?> res5 = entityManager.createNativeQuery(
                            "SELECT NoiDung, LoiGiai FROM so06_kiet_hung_thang_gio WHERE Thangsinh = ? AND GioID = ?")
                            .setParameter(1, thangSinh).setParameter(2, gioId).getResultList();
                    for (Object obj : res5) {
                        Object[] r = (Object[]) obj;
                        khMatches.put(r[0].toString(), r[1].toString());
                    }

                    // Tuổi + Tháng
                    List<?> res6 = entityManager.createNativeQuery(
                            "SELECT NoiDung, LoiGiai FROM so06_kiet_hung_tuoi_thang WHERE TuoiID = ? AND Thang = ?")
                            .setParameter(1, chiId).setParameter(2, thangSinh).getResultList();
                    for (Object obj : res6) {
                        Object[] r = (Object[]) obj;
                        khMatches.put(r[0].toString(), r[1].toString());
                    }
                } catch (Exception e) {
                }

                // Xuất kết quả: Có hoặc Không cho tất cả NoiDung dưới dạng danh sách gạch đầu
                // dòng
                content.append("<ul style='list-style-type: none; padding-left: 0;'>");
                for (String nd : allNoiDunSet) {
                    content.append("<li style='margin-bottom: 8px;'>");
                    content.append("• <b>").append(nd).append("</b>: ");
                    if (khMatches.containsKey(nd)) {
                        content.append("<span style='color: #2e7d32; font-weight: bold;'>Có</span> - ")
                                .append(khMatches.get(nd));
                    } else {
                        content.append("<span style='color: #d32f2f; font-weight: bold;'>Không</span>");
                    }
                    content.append("</li>");
                    foundContent = true;
                }
                content.append("</ul>");

            } catch (Exception e) {
                System.err.println("Lỗi Sở 6: " + e.getMessage());
            }
            if (foundContent)
                return;
        }

        // === SỞ 7: Coi Hồn đầu thai ===
        if (secNo == 7) {
            try {
                String genderVn = getGenderVn(request.getGender());
                String[] possibleTables = { "so07_hon_dau_thai", "so07_hondauthai", "so07_hon_dau_thai_12_cau" };

                for (String tableName : possibleTables) {
                    try {
                        List<?> allRows = entityManager.createNativeQuery("SELECT * FROM " + tableName).getResultList();
                        if (allRows.isEmpty()) continue;

                        List<Object[]> colsInfo = entityManager.createNativeQuery("SHOW COLUMNS FROM " + tableName).getResultList();
                        java.util.List<String> colNames = new java.util.ArrayList<>();
                        for (Object[] col : colsInfo) colNames.add(col[0].toString().toLowerCase());

                        int nhIdx = findColIndex(colNames, "nguhanhid", "ngu_hanhid", "mang_id");
                        int tsIdx = findColIndex(colNames, "thangsanh", "thang_sanh", "thang");
                        int gtIdx = findColIndex(colNames, "gioitinh", "gioi_tinh", "phai", "nam_nu");
                        int csIdx = findColIndex(colNames, "causo", "cau_so", "so_cau");
                        int lgIdx = findColIndex(colNames, "loigiai", "loi_giai", "ketqua", "noi_dung");

                        for (Object obj : allRows) {
                            Object[] row = (Object[]) obj;
                            
                            // Check match mangId
                            boolean matchNH = (nhIdx < 0) || (row[nhIdx] != null && row[nhIdx].toString().equals(String.valueOf(mangId)));
                            // Check match thangSinh
                            boolean matchTS = (tsIdx < 0) || (row[tsIdx] != null && row[tsIdx].toString().equals(String.valueOf(thangSinh)));
                            // Check match gender
                            boolean matchGT = (gtIdx < 0) || (row[gtIdx] != null && (row[gtIdx].toString().equalsIgnoreCase(genderVn) || row[gtIdx].toString().toLowerCase().contains(genderVn.toLowerCase())));

                            if (matchNH && matchTS && matchGT) {
                                String cauSoVal = (csIdx >= 0 && row[csIdx] != null) ? row[csIdx].toString() : "N/A";
                                String loiGiaiVal = (lgIdx >= 0 && row[lgIdx] != null) ? row[lgIdx].toString() : "N/A";

                                content.append("<p><strong>Mạng ").append(mang)
                                        .append(", cầu số ").append(cauSoVal).append("</strong><br>")
                                        .append(loiGiaiVal).append("</p>");
                                foundContent = true;
                                break;
                            }
                        }
                    } catch (Exception eInner) {}
                    if (foundContent) break;
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 7: " + e.getMessage());
            }
            if (foundContent) return;
        }

        // === SỞ 8: Coi giờ sanh (3 giai đoạn) ===
        if (secNo == 8) {
            try {
                String giaiDoan = getGiaiDoanGio(gioSinhFull);
                String chiGio = canChiGio;
                
                // Tìm TẤT CẢ bảng có tên bắt đầu bằng so08
                List<String> s8Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so08%'").getResultList();
                if (s8Tables.isEmpty()) {
                    // Thử prefix khác
                    s8Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so_08%'").getResultList();
                }

                for (String tableName : s8Tables) {
                    try {
                        List<Object[]> colsInfo = entityManager.createNativeQuery("SHOW COLUMNS FROM `" + tableName + "`").getResultList();
                        java.util.List<String> colNames = new java.util.ArrayList<>();
                        for (Object[] col : colsInfo) colNames.add(col[0].toString().toLowerCase());

                        int chiColIdx = findColIndex(colNames, "chiid", "chi_id", "gio_id", "ten_gio_chi", "tuoi_chiid");
                        int gdColIdx = findColIndex(colNames, "giaidoan", "giai_doan", "phan", "segment", "buoi_sanh");
                        int lgColIdx = findColIndex(colNames, "loigiai", "loi_giai", "ketqua", "noi_dung", "mota");

                        // Xây dựng query filter theo Chi và GiaiDoan
                        StringBuilder sql = new StringBuilder("SELECT * FROM `").append(tableName).append("` WHERE 1=1 ");
                        List<Object> params = new java.util.ArrayList<>();
                        
                        if (chiColIdx >= 0) {
                            sql.append(" AND `").append(colNames.get(chiColIdx)).append("` = ? ");
                            params.add(getChiId(chiGio));
                        }
                        // Nếu có cột GiaiDoan, lọc đúng segment
                        if (gdColIdx >= 0) {
                            sql.append(" AND (`").append(colNames.get(gdColIdx)).append("` LIKE ? OR `")
                               .append(colNames.get(gdColIdx)).append("` LIKE ? ) ");
                            params.add("%" + giaiDoan + "%");
                            params.add("%" + (giaiDoan.equals("Sau") ? "Cuối" : giaiDoan) + "%");
                        }

                        var query = entityManager.createNativeQuery(sql.toString());
                        for (int i=0; i<params.size(); i++) query.setParameter(i+1, params.get(i));
                        List<?> rows = query.getResultList();

                        if (!rows.isEmpty()) {
                            // Nếu có nhiều hơn 1 row (do like filter), ưu tiên row khớp nhất hoặc lấy row đầu
                            Object[] row = (Object[]) rows.get(0);
                            String loiGiaiVal = "N/A";
                            if (lgColIdx >= 0 && row[lgColIdx] != null) {
                                loiGiaiVal = row[lgColIdx].toString().replace("\n", "<br>");
                            } else {
                                // Nếu không tìm thấy cột lời giải đích danh, lấy cột text dài nhất
                                int maxLen = -1;
                                for (Object cell : row) {
                                    if (cell != null && cell.toString().length() > maxLen) {
                                        maxLen = cell.toString().length();
                                        loiGiaiVal = cell.toString().replace("\n", "<br>");
                                    }
                                }
                            }
                            
                            content.append("<p><strong>").append(giaiDoan).append(" giờ ").append(chiGio)
                                   .append("</strong><br>").append(loiGiaiVal).append("</p>");
                            foundContent = true;
                            // Quan trọng: return để KHÔNG chạy logic dynamic scanner phía dưới
                            return; 
                        }
                    } catch (Exception eInner) {
                        System.err.println("Lỗi query bảng " + tableName + ": " + eInner.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 8: " + e.getMessage());
            }
        }
        
        // === SỞ 9: Coi ngày sanh (Ngày) ===
        if (secNo == 9) {
            try {
                List<String> s9Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so09%'").getResultList();
                if (s9Tables.isEmpty()) {
                    s9Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so_09%'").getResultList();
                }
                for (String tableName : s9Tables) {
                    try {
                        List<Object[]> colsInfo = entityManager.createNativeQuery("SHOW COLUMNS FROM `" + tableName + "`").getResultList();
                        java.util.List<String> colNames = new java.util.ArrayList<>();
                        for (Object[] col : colsInfo) colNames.add(col[0].toString().toLowerCase());

                        int dayColIdx = findColIndex(colNames, "ngay", "ngaysanh", "ngay_sanh", "ngay_id");
                        int starColIdx = findColIndex(colNames, "tensao", "ten_sao", "hieu_ngay", "sao", "tieu_de");
                        int lgColIdx = findColIndex(colNames, "loigiai", "loi_giai", "ketqua", "noi_dung", "mota");

                        String sql = "SELECT * FROM `" + tableName + "` WHERE `" + colNames.get(dayColIdx >= 0 ? dayColIdx : 0) + "` = ?";
                        var query = entityManager.createNativeQuery(sql);
                        query.setParameter(1, ngaySinh);
                        List<?> rows = query.getResultList();

                        if (!rows.isEmpty()) {
                            Object[] row = (Object[]) rows.get(0);
                            String tenSao = (starColIdx >= 0 && row[starColIdx] != null) ? row[starColIdx].toString() : "N/A";
                            String loiGiaiVal = (lgColIdx >= 0 && row[lgColIdx] != null) ? row[lgColIdx].toString() : "N/A";
                            
                            content.append("<p><strong>Ngày ").append(ngaySinh).append(": ").append(tenSao)
                                   .append("</strong><br>").append(loiGiaiVal).append("</p>");
                            foundContent = true;
                            return;
                        }
                    } catch (Exception eInner) {}
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 9: " + e.getMessage());
            }
        }
        
        // === SỞ 10: Thọ thai sanh (Tháng thọ thai & tháng sanh) ===
        if (secNo == 10) {
            try {
                List<String> s10Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so10%'").getResultList();
                if (s10Tables.isEmpty()) {
                    s10Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so_10%'").getResultList();
                }

                for (String tableName : s10Tables) {
                    try {
                        List<Object[]> colsInfo = entityManager.createNativeQuery("SHOW COLUMNS FROM `" + tableName + "`").getResultList();
                        java.util.List<String> colNames = new java.util.ArrayList<>();
                        for (Object[] col : colsInfo) colNames.add(col[0].toString().toLowerCase());

                        // Tìm cột Tháng Thọ Thai (ưu tiên các từ khóa liên quan thọ thai)
                        int thoColIdx = findColIndex(colNames, "thang_tho_thai", "thothai", "tho_thai");
                        // Tìm cột Tháng Sanh (ưu tiên các từ khóa liên quan sanh)
                        int sanhColIdx = findColIndex(colNames, "thang_sanh", "thangsanh", "sanh_thang", "thang");
                        // Cột Lời Giải
                        int lgColIdx = findColIndex(colNames, "loigiai", "loi_giai", "ketqua", "noi_dung", "mota");

                        if (thoColIdx < 0 && sanhColIdx < 0) continue; // Không phải bảng chuẩn

                        String sql = "SELECT * FROM `" + tableName + "` WHERE 1=1 ";
                        List<Object> params = new java.util.ArrayList<>();
                        if (thoColIdx >= 0) {
                            sql += " AND `" + colNames.get(thoColIdx) + "` = ? ";
                            params.add(thangThoThai);
                        }
                        if (sanhColIdx >= 0) {
                            sql += " AND `" + colNames.get(sanhColIdx) + "` = ? ";
                            params.add(thangSinh);
                        }

                        var query = entityManager.createNativeQuery(sql);
                        for (int i=0; i<params.size(); i++) query.setParameter(i+1, params.get(i));
                        List<?> rows = query.getResultList();

                        if (!rows.isEmpty()) {
                            Object[] row = (Object[]) rows.get(0);
                            String loiGiaiVal = "N/A";
                            if (lgColIdx >= 0 && row[lgColIdx] != null) {
                                loiGiaiVal = row[lgColIdx].toString().replace("\n", "<br>");
                            } else {
                                // Fallback: tìm text dài nhất
                                int maxLen = -1;
                                for (Object cell : row) {
                                    if (cell != null && cell.toString().length() > maxLen) {
                                        maxLen = cell.toString().length();
                                        loiGiaiVal = cell.toString().replace("\n", "<br>");
                                    }
                                }
                            }
                            
                            content.append("<p><strong>Thọ thai tháng ").append(thangThoThai).append(" sanh tháng ").append(thangSinh)
                                   .append("</strong><br>").append(loiGiaiVal).append("</p>");
                            foundContent = true;
                            return; // Dừng lại ở bảng đầu tiên có dữ liệu
                        }
                    } catch (Exception eInner) {}
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 10: " + e.getMessage());
            }
        }
        
        // === SỞ 11: Coi nghề nghiệp (Mạng & Tháng sanh) ===
        if (secNo == 11) {
            try {
                List<String> s11Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so11%'").getResultList();
                if (s11Tables.isEmpty()) {
                    s11Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so_11%'").getResultList();
                }
                for (String tableName : s11Tables) {
                    try {
                        List<Object[]> colsInfo = entityManager.createNativeQuery("SHOW COLUMNS FROM `" + tableName + "`").getResultList();
                        java.util.List<String> colNames = new java.util.ArrayList<>();
                        for (Object[] col : colsInfo) colNames.add(col[0].toString().toLowerCase());

                        int nhColIdx = findColIndex(colNames, "nguhanhid", "ngu_hanhid", "mang", "mang_id");
                        int tsColIdx = findColIndex(colNames, "thangsanh", "thang_sanh", "thang");
                        int lgColIdx = findColIndex(colNames, "nhom_nghe", "nhomnghe", "loigiai", "ketqua", "noi_dung", "mota");

                        String sql = "SELECT * FROM `" + tableName + "` WHERE `" + colNames.get(nhColIdx >= 0 ? nhColIdx : 0) + "` = ? AND `" + colNames.get(tsColIdx >= 0 ? tsColIdx : 1) + "` = ?";
                        var query = entityManager.createNativeQuery(sql);
                        query.setParameter(1, mangId);
                        query.setParameter(2, thangSinh);
                        List<?> rows = query.getResultList();

                        if (!rows.isEmpty()) {
                            content.append("<p><strong>Mạng ").append(mang).append(" sinh tháng ").append(thangSinh).append("</strong></p>");
                            for (Object objRow : rows) {
                                Object[] row = (Object[]) objRow;
                                String loiGiaiVal = "N/A";
                                if (lgColIdx >= 0 && row[lgColIdx] != null) {
                                    loiGiaiVal = row[lgColIdx].toString().replace("\n", "<br>");
                                } else {
                                    // Fallback tìm text dài nhất
                                    int maxLen = -1;
                                    for (Object cell : row) {
                                        if (cell != null && cell.toString().length() > maxLen) {
                                            maxLen = cell.toString().length();
                                            loiGiaiVal = cell.toString().replace("\n", "<br>");
                                        }
                                    }
                                }
                                content.append("<div style='margin-bottom:8px;'>• ").append(loiGiaiVal).append("</div>");
                            }
                            foundContent = true;
                            return;
                        }
                    } catch (Exception eInner) {}
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 11: " + e.getMessage());
            }
        }
        
        // === SỞ 12: Coi cốt con gì (Tuổi & Tháng sanh) ===
        if (secNo == 12) {
            try {
                List<String> s12Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so12%'").getResultList();
                if (s12Tables.isEmpty()) {
                    s12Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so_12%'").getResultList();
                }
                for (String tableName : s12Tables) {
                    try {
                        List<Object[]> colsInfo = entityManager.createNativeQuery("SHOW COLUMNS FROM `" + tableName + "`").getResultList();
                        java.util.List<String> colNames = new java.util.ArrayList<>();
                        for (Object[] col : colsInfo) colNames.add(col[0].toString().toLowerCase());

                        int chiColIdx = findColIndex(colNames, "chiid", "tuoi_chiid", "chi", "chi_id");
                        int tsColIdx = findColIndex(colNames, "thangsanh", "thang_sanh", "thang");
                        int cotColIdx = findColIndex(colNames, "tencot", "ten_cot", "cot", "ketqua");
                        int lgColIdx = findColIndex(colNames, "loigiai", "loi_giai", "noi_dung", "mota");

                        String sql = "SELECT * FROM `" + tableName + "` WHERE `" + colNames.get(chiColIdx >= 0 ? chiColIdx : 0) + "` = ? AND `" + colNames.get(tsColIdx >= 0 ? tsColIdx : 1) + "` = ?";
                        var query = entityManager.createNativeQuery(sql);
                        query.setParameter(1, chiId);
                        query.setParameter(2, thangSinh);
                        List<?> rows = query.getResultList();

                        if (!rows.isEmpty()) {
                            Object[] row = (Object[]) rows.get(0);
                            String tenCot = (cotColIdx >= 0 && row[cotColIdx] != null) ? row[cotColIdx].toString() : "N/A";
                            String loiGiaiVal = (lgColIdx >= 0 && row[lgColIdx] != null) ? row[lgColIdx].toString().replace("\n", "<br>") : "N/A";
                            
                            content.append("<p><strong>Tuổi ").append(chi).append(" sinh tháng ").append(thangSinh)
                                   .append(": ").append(tenCot).append("</strong><br>")
                                   .append(loiGiaiVal).append("</p>");
                            foundContent = true;
                            return;
                        }
                    } catch (Exception eInner) {}
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 12: " + e.getMessage());
            }
        }

        // === SỞ 13: Coi số làm ăn (Can/Chi & Tháng sanh) ===
        if (secNo == 13) {
            try {
                // Tiêu đề phần Can
                content.append("<p><strong>Can ").append(can).append(" Sinh Tháng ").append(thangSinh).append("</strong></p>");
                
                // 1. so13_nghenghiep
                appendSo13TableResult(content, "so13_nghenghiep", "CanID", canId, thangSinh);
                // 2. so13_can_sanghen
                appendSo13TableResult(content, "so13_can_sanghen", "CanID", canId, thangSinh);
                // 3. so13_can_thang_sanh (Lưu ý: bảng này dùng CanID dạng chữ: Giáp, Ất...)
                appendSo13TableResult(content, "so13_can_thang_sanh", "CanID", getCanName(canId), thangSinh);
                // 4. so13_can_loc
                appendSo13TableResult(content, "so13_can_loc", "CanID", canId, thangSinh);

                content.append("<br>");

                // Tiêu đề phần Chi
                content.append("<p><strong>Chi ").append(chi).append(" Sinh Tháng ").append(thangSinh).append("</strong></p>");
                
                // 5. so13_chi_sat
                appendSo13TableResult(content, "so13_chi_sat", "ChiID", chiId, thangSinh);
                // 6. so13_chi_thu
                appendSo13TableResult(content, "so13_chi_thu", "ChiID", chiId, thangSinh);
                
                foundContent = true;
                return;
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 13: " + e.getMessage());
            }
        }

        // === SỞ 14: Coi nuôi thú vật (Tuổi & Tháng sanh) ===
        if (secNo == 14) {
            try {
                // Table: so14_nuoithuvat (ChiID, ThangSanh, KetQua, LoiGiai)
                String sql = "SELECT KetQua, LoiGiai FROM so14_nuoithuvat WHERE ChiID = ? AND ThangSanh = ? LIMIT 1";
                List<?> results = entityManager.createNativeQuery(sql)
                        .setParameter(1, chiId).setParameter(2, thangSinh).getResultList();
                
                if (!results.isEmpty()) {
                    Object[] row = (Object[]) results.get(0);
                    String ketQua = row[0] != null ? row[0].toString() : "";
                    String loiGiai = row[1] != null ? row[1].toString() : "";

                    // Chỉ in ra KetQua + LoiGiai theo yêu cầu
                    content.append("<p><strong>").append(ketQua).append("</strong> ")
                           .append(loiGiai.replace("\n", "<br>")).append("</p>");
                    foundContent = true;
                    return;
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 14: " + e.getMessage());
            }
        }

        // === SỞ 15: Coi ruộng đất (Mạng & Tháng sanh) ===
        if (secNo == 15) {
            try {
                // Table: so15_ruongdat (NguHanhID, ThangSanh, KetQua, LoiGiai)
                String sql = "SELECT KetQua, LoiGiai FROM so15_ruongdat WHERE NguHanhID = ? AND ThangSanh = ? LIMIT 1";
                List<?> results = entityManager.createNativeQuery(sql)
                        .setParameter(1, mangId).setParameter(2, thangSinh).getResultList();
                
                if (!results.isEmpty()) {
                    Object[] row = (Object[]) results.get(0);
                    String ketQua = row[0] != null ? row[0].toString() : "";
                    String loiGiai = row[1] != null ? row[1].toString() : "";

                    content.append("<p><strong>").append(ketQua).append("</strong> ")
                           .append(loiGiai.replace("\n", "<br>")).append("</p>");
                    foundContent = true;
                    return;
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 15: " + e.getMessage());
            }
        }

        // === SỞ 16: Coi học giỏi dở (Tuổi & Tháng sanh) ===
        if (secNo == 16) {
            try {
                // Table: so16_hocgioido (ChiID, ThangSanh, TenTruc, LoiGiai)
                String sql = "SELECT TenTruc, LoiGiai FROM so16_hocgioido WHERE ChiID = ? AND ThangSanh = ? LIMIT 1";
                List<?> results = entityManager.createNativeQuery(sql)
                        .setParameter(1, chiId).setParameter(2, thangSinh).getResultList();
                
                if (!results.isEmpty()) {
                    Object[] row = (Object[]) results.get(0);
                    String tenTruc = row[0] != null ? row[0].toString() : "";
                    String loiGiai = row[1] != null ? row[1].toString() : "";

                    content.append("<p><strong>").append(tenTruc).append("</strong> ")
                           .append(loiGiai.replace("\n", "<br>")).append("</p>");
                    foundContent = true;
                    return;
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 16: " + e.getMessage());
            }
        }

        // === SỞ 17: Coi thi cử (Tuổi & Tháng sanh) ===
        if (secNo == 17) {
            try {
                // Tiêu đề Kỳ nhất
                content.append("<p><strong>Thi cử kỳ nhất</strong></p>");
                
                // Query Kỳ nhất (dùng SELECT * và tìm cột động để tránh sai sót)
                List<?> res1 = entityManager.createNativeQuery("SELECT * FROM so17_thicu_kynhat WHERE ChiID = ? AND ThangSanh = ?")
                        .setParameter(1, chiId).setParameter(2, thangSinh).getResultList();
                
                if (res1.isEmpty()) {
                    content.append("<p>Số này không có luận giải cho kỳ thi này.</p>");
                } else {
                    List<String> cols1 = getTableColumnNames("so17_thicu_kynhat");
                    int tenIdx = -1, lgIdx = -1;
                    for(int i=0; i<cols1.size(); i++){
                        String c = cols1.get(i).toLowerCase();
                        if(c.contains("ten") || c.contains("tu")) tenIdx = i;
                        if(c.contains("loigiai")) lgIdx = i;
                    }
                    for (Object obj : res1) {
                        Object[] row = (Object[]) obj;
                        String ten = (tenIdx >= 0 && row[tenIdx] != null) ? row[tenIdx].toString() : "";
                        String lg = (lgIdx >= 0 && row[lgIdx] != null) ? row[lgIdx].toString() : "";
                        content.append("<p><b>").append(ten).append("</b> ").append(lg.replace("\n", "<br>")).append("</p>");
                    }
                }

                // Tiêu đề Kỳ nhì
                content.append("<p><strong>Thi cử kỳ nhì</strong></p>");
                
                // Query Kỳ nhì
                List<?> res2 = entityManager.createNativeQuery("SELECT * FROM so17_thicu_kynhi WHERE ChiID = ? AND ThangSanh = ?")
                        .setParameter(1, chiId).setParameter(2, thangSinh).getResultList();
                
                if (res2.isEmpty()) {
                    content.append("<p>Số này không có luận giải cho kỳ thi này.</p>");
                } else {
                    List<String> cols2 = getTableColumnNames("so17_thicu_kynhi");
                    int tenIdx2 = -1, lgIdx2 = -1;
                    for(int i=0; i<cols2.size(); i++){
                        String c = cols2.get(i).toLowerCase();
                        if(c.contains("ten") || c.contains("ketqua")) tenIdx2 = i;
                        if(c.contains("loigiai")) lgIdx2 = i;
                    }
                    for (Object obj : res2) {
                        Object[] row = (Object[]) obj;
                        String ten = (tenIdx2 >= 0 && row[tenIdx2] != null) ? row[tenIdx2].toString() : "";
                        String lg = (lgIdx2 >= 0 && row[lgIdx2] != null) ? row[lgIdx2].toString() : "";
                        content.append("<p><b>").append(ten).append("</b> ").append(lg.replace("\n", "<br>")).append("</p>");
                    }
                }
                
                foundContent = true;
                return;
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 17: " + e.getMessage());
            }
        }
        if (secNo == 20) {
            try {
                // Bước 1: Tra ma trận duyên nợ (dùng NguHanhID của chồng + ThangSanh của vợ)
                String sql1 = "SELECT KetQua FROM so20_ma_tran_duyen_no WHERE (Chong_NguHanhID = ? OR chong_ngu_hanhid = ?) AND (Vo_ThangSanh = ? OR vo_thang_sanh = ?) LIMIT 1";
                List<?> kqList = entityManager.createNativeQuery(sql1)
                        .setParameter(1, mangId).setParameter(2, mangId)
                        .setParameter(3, thangSinh).setParameter(4, thangSinh)
                        .getResultList();
                if (!kqList.isEmpty()) {
                    String ketQua = kqList.get(0).toString();
                    // Bước 2: Tra lời giải từ KetQua
                    String sql2 = "SELECT LoiGiai FROM so20_loi_giai_duyen_no WHERE (KetQua = ? OR ket_qua = ?) LIMIT 1";
                    List<?> lgList = entityManager.createNativeQuery(sql2)
                            .setParameter(1, ketQua).setParameter(2, ketQua)
                            .getResultList();
                    content.append("<div style='margin-bottom: 10px;'><h6 style='color:#b8860b'>▶ Duyên nợ: ")
                            .append(ketQua).append("</h6>");
                    for (Object lg : lgList) {
                        if (lg != null) {
                            content.append("<p>").append(lg.toString().replace("\n", "<br>")).append("</p>");
                        }
                    }
                    content.append("</div>");
                    foundContent = true;
                }

                // Tháng xung khắc
                String sql3 = "SELECT ThangPham FROM so20_thang_xungkhac WHERE ChiID = ? AND GioiTinh = ? LIMIT 1";
                String genderVn = getGenderVn(request.getGender());
                List<?> xkList = entityManager.createNativeQuery(sql3)
                        .setParameter(1, chiId).setParameter(2, genderVn)
                        .getResultList();
                if (!xkList.isEmpty() && xkList.get(0) != null) {
                    content.append("<p><strong>Tháng xung khắc:</strong> ").append(xkList.get(0).toString())
                            .append("</p>");
                    foundContent = true;
                }
            } catch (Exception e) {
                System.err.println("Lỗi Sở 20: " + e.getMessage());
            }
            if (!foundContent) {
                content.append(
                        "<p><em>(Chúc mừng bạn, theo sách Diễn Cầm Tam Thế, tuổi và tháng sinh của bạn không phạm phải tai ương này)</em></p>");
            }
            return;
        }

        // === SỞ 21: Vòng Trường Sinh ===
        if (secNo == 21) {
            try {
                if (truongSanhId > 0 && truongSanhId <= 12) {
                    String[] tsNames = { "", "Trường Sanh", "Mộc Dục", "Quan Đới", "Lâm Quan", "Đế Vượng", "Suy",
                            "Bệnh", "Tử", "Mộ", "Tuyệt", "Thai", "Dưỡng" };
                    content.append("<p>Vòng Trường Sinh bản mệnh của bạn là: <strong>").append(tsNames[truongSanhId])
                            .append("</strong></p>");
                    foundContent = true;
                } else {
                    content.append(
                            "<p><em>(Chưa tính được Vòng Trường Sinh vì thiếu dữ liệu tuổi/mệnh trùng khớp)</em></p>");
                    foundContent = true;
                }
            } catch (Exception e) {
                System.err.println("Lỗi Sở 21: " + e.getMessage());
            }
            return;
        }

        // === SỞ 22: Nuôi con (cần TruongSanhID từ Sở 21) ===
        if (secNo == 22) {
            if (truongSanhId <= 0) {
                content.append("<p><em>(Số này chưa tính được Vòng Trường Sinh - Không thể tra Sở 22)</em></p>");
            } else {
                try {
                    List<?> results = entityManager.createNativeQuery("SELECT * FROM so22_nuoicon WHERE TruongSanhID = ?")
                            .setParameter(1, truongSanhId).getResultList();
                    
                    if (!results.isEmpty()) {
                        List<String> colNames = getTableColumnNames("so22_nuoicon");
                        int tenIdx = -1, lgIdx = -1;
                        for (int i = 0; i < colNames.size(); i++) {
                            String lower = colNames.get(i).toLowerCase();
                            if (lower.contains("ten") || lower.contains("sao")) tenIdx = i;
                            if (lower.contains("loigiai")) lgIdx = i;
                        }

                        for (Object obj : results) {
                            Object[] row = (Object[]) obj;
                            String ten = (tenIdx >= 0 && row[tenIdx] != null) ? row[tenIdx].toString() : "";
                            String lg = (lgIdx >= 0 && row[lgIdx] != null) ? row[lgIdx].toString() : "";
                            
                            content.append("<p><strong>").append(ten).append("</strong><br>")
                                   .append(lg.replace("\n", "<br>")).append("</p>");
                            foundContent = true;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi manual Sở 22: " + e.getMessage());
                }
            }
            if (foundContent) return;
        }

        // === SỞ 23: Anh em (cần TruongSanhID từ Sở 21) ===
        if (secNo == 23) {
            if (truongSanhId <= 0) {
                content.append("<p><em>(Số này chưa tính được Vòng Trường Sinh - Không thể tra Sở 23)</em></p>");
            } else {
                try {
                    List<?> results = entityManager.createNativeQuery("SELECT * FROM so23_anhem WHERE TruongSanhID = ?")
                            .setParameter(1, truongSanhId).getResultList();
                    
                    if (!results.isEmpty()) {
                        List<String> colNames = getTableColumnNames("so23_anhem");
                        int tenIdx = -1, thoIdx = -1;
                        for (int i = 0; i < colNames.size(); i++) {
                            String lower = colNames.get(i).toLowerCase();
                            if (lower.contains("ten") || lower.contains("sao")) tenIdx = i;
                            if (lower.contains("baitho")) thoIdx = i;
                        }

                        for (Object obj : results) {
                            Object[] row = (Object[]) obj;
                            String ten = (tenIdx >= 0 && row[tenIdx] != null) ? row[tenIdx].toString() : "";
                            String tho = (thoIdx >= 0 && row[thoIdx] != null) ? row[thoIdx].toString() : "";
                            
                            content.append("<div style='margin-bottom:20px; text-align: left;'>");
                            content.append("<p style='text-align: center;'><strong>").append(ten).append(":</strong></p>");
                            
                            String[] lines = tho.split("\n");
                            for (int k = 0; k < lines.length; k++) {
                                String lineText = lines[k].trim();
                                if (lineText.isEmpty()) continue;
                                if (k % 2 != 0) {
                                    // Dòng 8: Không thụt lề, căn giữa
                                    content.append("<div style='padding: 0 0px; margin-bottom: 5px; text-align: center;'>").append(lineText).append("</div>");
                                } else {
                                    // Dòng 6: Thụt lề 20px hai bên, căn giữa
                                    content.append("<div style='padding: 0 20px; margin-bottom: 5px; text-align: center;'>").append(lineText).append("</div>");
                                }
                            }
                            content.append("</div>");
                            foundContent = true;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi manual Sở 23: " + e.getMessage());
                }
            }
            if (foundContent) return;
        }

        // === SỞ 30: Lời khuyên & Ngày cầu Tiên Bà ===
        if (secNo == 30) {
            try {
                // PART 1: so30_loikhuyen
                String sql1 = "SELECT NoiDung FROM so30_loikhuyen LIMIT 1";
                List<?> res1 = entityManager.createNativeQuery(sql1).getResultList();
                if (!res1.isEmpty()) {
                    content.append("<p><strong>Lời dặn</strong><br>");
                    content.append(res1.get(0).toString()).append("</p>");
                    foundContent = true;
                }

                // PART 2: so30_ngay_cau_tien
                String sql2 = "SELECT LoiGiai FROM so30_ngay_cau_tien WHERE ChiID = ?";
                List<?> res2 = entityManager.createNativeQuery(sql2)
                        .setParameter(1, chiId).getResultList();
                if (!res2.isEmpty()) {
                    content.append("<p><strong>Ngày cầu Tiên Bà cứu bịnh</strong><br>");
                    content.append(res2.get(0).toString().replace("\n", "<br>")).append("</p>");
                    foundContent = true;
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 30: " + e.getMessage());
            }
            if (foundContent) return;
        }

        // === SỞ 34-37: Mega section (Triết lý, Nhân quả, Ngũ tạng, Lời kết) ===
        if (secNo == 34 || secNo == 35 || secNo == 36 || secNo == 37) {
            // Because these are combined into section 34, we must "consume" 35, 36, 37 
            // by appending a hidden marker so the generic logic doesn't run.
            if (secNo != 34) {
                content.append("<!-- Handled in Sở 34 -->");
                return;
            }

            try {
                // 1. Sở 34: Triết lý & Ngũ phương
                content.append("<h3 style='color: #d32f2f; text-align: center; border-bottom: 2px solid #d32f2f; padding-bottom: 5px;'>SỞ 34: COI NGŨ PHƯƠNG & TRIẾT LÝ</h3>");
                List<Object[]> trietly = entityManager.createNativeQuery("SELECT TieuDe, NoiDung FROM so34_trietly").getResultList();
                for (Object[] row : trietly) {
                    content.append("<p><strong>").append(row[0]).append("</strong><br>")
                           .append(row[1].toString().replace("\n", "<br>")).append("</p>");
                }

                // Ngũ phương content
                List<Object[]> nguphuong = entityManager.createNativeQuery("SELECT TenDe, NguHanh, MauSac FROM so34_nguphuong").getResultList();
                for (Object[] row : nguphuong) {
                    content.append("<p>• <strong>").append(row[0]).append("</strong>: ")
                           .append(row[1]).append(" (").append(row[2]).append(")</p>");
                }

                // 2. Sở 35: Nhân quả
                content.append("<h3 style='color: #d32f2f; text-align: center; border-bottom: 2px solid #d32f2f; padding-bottom: 5px; margin-top: 30px;'>SỞ 35: NHÂN QUẢ</h3>");
                List<Object[]> nhanqua = entityManager.createNativeQuery("SELECT BoPhan, HanhVi_Nhan, KetQua_Qua FROM so35_nhan_qua").getResultList();
                for (Object[] row : nhanqua) {
                    content.append("<p>• <strong>").append(row[0]).append("</strong>: ")
                           .append(row[1]).append(" &rarr; <em>").append(row[2]).append("</em></p>");
                }

                // 3. Sở 36: Ngũ tạng
                content.append("<h3 style='color: #d32f2f; text-align: center; border-bottom: 2px solid #d32f2f; padding-bottom: 5px; margin-top: 30px;'>SỞ 36: NGŨ TẠNG</h3>");
                List<Object[]> ngutang = entityManager.createNativeQuery("SELECT TangPhu, NguHanh, MoTa FROM so36_ngu_tang").getResultList();
                for (Object[] row : ngutang) {
                    content.append("<p><strong>").append(row[0]).append("</strong> (").append(row[1]).append(")<br>")
                           .append(row[2]).append("</p>");
                }

                // 4. Sở 37: Lời kết
                content.append("<h3 style='color: #d32f2f; text-align: center; border-bottom: 2px solid #d32f2f; padding-bottom: 5px; margin-top: 30px;'>SỞ 37: LỜI KẾT</h3>");
                List<Object[]> loiket = entityManager.createNativeQuery("SELECT TieuDe, NoiDung FROM so37_loi_ket").getResultList();
                for (Object[] row : loiket) {
                    content.append("<p><strong>").append(row[0]).append("</strong><br>")
                           .append(row[1].toString().replace("\n", "<br>")).append("</p>");
                }

                foundContent = true;
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 34 mega: " + e.getMessage());
            }
            if (foundContent) return;
        }
        if (secNo == 24) {
            try {
                String sql1 = "SELECT ViTri FROM so24_huynhde_mapping WHERE mua = ? AND ChiID = ?";
                List<?> vitriList = entityManager.createNativeQuery(sql1)
                        .setParameter(1, mua).setParameter(2, chiId).getResultList();
                
                if (!vitriList.isEmpty()) {
                    String vitri = vitriList.get(0).toString();
                    content.append("<p style='text-align: center; margin-bottom: 15px;'>")
                           .append("<span style='color: #1a5fb4; font-weight: bold; font-size: 1.1em;'>Mùa ").append(mua).append(" tuổi ").append(chi).append("</span><br>")
                           .append("<span style='color: #b8860b; font-weight: bold; font-size: 1.1em;'>Ở tại ").append(vitri).append("</span>")
                           .append("</p>");

                    String sql2 = "SELECT BaiTho FROM so24_huynhde_loigiai WHERE ViTri = ?";
                    List<?> thoList = entityManager.createNativeQuery(sql2)
                            .setParameter(1, vitri).getResultList();
                    
                    for (Object obj : thoList) {
                        if (obj != null) {
                            String tho = obj.toString();
                            String[] lines = tho.split("\n");
                            for (int k = 0; k < lines.length; k++) {
                                String lineText = lines[k].trim();
                                if (lineText.isEmpty()) continue;
                                if (k % 2 != 0) {
                                    // Dòng 8
                                    content.append("<div style='padding: 0 0px; margin-bottom: 5px; text-align: center;'>").append(lineText).append("</div>");
                                } else {
                                    // Dòng 6
                                    content.append("<div style='padding: 0 20px; margin-bottom: 5px; text-align: center;'>").append(lineText).append("</div>");
                                }
                            }
                        }
                    }
                    foundContent = true;
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 24: " + e.getMessage());
            }
            if (foundContent) return;
        }

        // === SỞ 27: Tuổi hạp, kỵ (Tổng quan, Tháng, Ngày) ===
        if (secNo == 27) {
            try {
                String genderVn = getGenderVn(request.getGender());
                
                // 1. so27_tongquan
                List<?> res1 = entityManager.createNativeQuery("SELECT Tuoi_CanID, Tuoi_ChiID, Mang, Luan, Tho FROM so27_tongquan WHERE Tuoi_CanID = ? AND Tuoi_ChiID = ? AND GioiTinh = ?")
                        .setParameter(1, canId).setParameter(2, chiId).setParameter(3, genderVn).getResultList();
                
                for (Object obj : res1) {
                    Object[] row = (Object[]) obj;
                    String cName = getCanName((int)row[0]);
                    String chName = getChiName((int)row[1]);
                    String mVal = row[2] != null ? row[2].toString() : "";
                    String lVal = row[3] != null ? row[3].toString() : "";
                    String tVal = row[4] != null ? row[4].toString() : "";

                    content.append("<p style='line-height:1.6;'>")
                           .append("<span style='color:#d32f2f; font-weight:bold;'>Tuổi: </span>").append(cName).append(" ").append(chName).append("<br>")
                           .append("<span style='color:#1a5fb4; font-weight:bold;'>Mạng: </span>").append(mVal).append("<br>")
                           .append("<span style='color:#7b1fa2; font-weight:bold;'>Luận: </span>").append(lVal).append("<br>")
                           .append("<span style='color:#2e7d32; font-weight:bold;'>Thơ: </span>")
                           .append("</p>");
                    
                    if (!tVal.isEmpty()) {
                        String[] lines = tVal.split("\n");
                        content.append("<div style='margin-bottom: 20px;'>");
                        for (int k = 0; k < lines.length; k++) {
                            String lt = lines[k].trim();
                            if (lt.isEmpty()) continue;
                            if (k % 2 != 0) {
                                // Dòng 8: padding 0
                                content.append("<div style='padding: 0 0px; margin-bottom: 5px; text-align: center; font-style: italic;'>").append(lt).append("</div>");
                            } else {
                                // Dòng 6: padding 20px
                                content.append("<div style='padding: 0 20px; margin-bottom: 5px; text-align: center; font-style: italic;'>").append(lt).append("</div>");
                            }
                        }
                        content.append("</div>");
                    }
                    foundContent = true;
                }

                // 2. so27_thangkyhap
                List<?> res2 = entityManager.createNativeQuery("SELECT TuoiApDung, DieuHap, DieuKy FROM so27_thangkyhap WHERE Tuoi_CanID = ? AND Tuoi_ChiID = ? AND GioiTinh = ? AND ThangSanh = ?")
                        .setParameter(1, canId).setParameter(2, chiId).setParameter(3, genderVn).setParameter(4, thangSinh).getResultList();
                for (Object obj : res2) {
                    Object[] row = (Object[]) obj;
                    content.append("<div style='margin-top:15px; background: rgba(26, 95, 180, 0.05); padding: 10px; border-radius: 8px;'>")
                           .append("<p style='margin:0;'>")
                           .append("<strong>Tuổi áp dụng: </strong>").append(row[0] != null ? row[0].toString() : "").append("<br>")
                           .append("<strong>Điều Hạp: </strong>").append(row[1] != null ? row[1].toString() : "").append("<br>")
                           .append("<strong>Điều Kỵ: </strong>").append(row[2] != null ? row[2].toString() : "").append("</p></div>");
                    foundContent = true;
                }

                // 3. so27_ngaykyhap
                try {
                    int d = Integer.parseInt(request.getBirthDay());
                    int m = Integer.parseInt(request.getBirthMonth());
                    int y = Integer.parseInt(request.getBirthYear());
                    int nChiId = calculateDayChiId(d, m, y);
                    
                    List<?> res3 = entityManager.createNativeQuery("SELECT LoiGiai FROM so27_ngaykyhap WHERE Tuoi_CanID = ? AND Tuoi_ChiID = ? AND Ngay_ChiID = ?")
                            .setParameter(1, canId).setParameter(2, chiId).setParameter(3, nChiId).getResultList();
                    for (Object obj : res3) {
                        content.append("<div style='margin-top:15px; border-left: 4px solid #d32f2f; padding-left: 10px;'>")
                               .append("<p style='margin:0;'>").append(obj.toString().replace("\n", "<br>")).append("</p></div>");
                        foundContent = true;
                    }
                } catch (Exception eDay) {}

            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 27: " + e.getMessage());
            }
            if (foundContent) return;
        }


        // === SỞ 32: Coi khi chết ===
        if (secNo == 32) {
            try {
                // so32_coi_khi_chet
                String sql1 = "SELECT loi_giai FROM so32_coi_khi_chet WHERE Mang = ? AND ThangSanh = ?";
                List<?> res1 = entityManager.createNativeQuery(sql1)
                        .setParameter(1, mangId).setParameter(2, thangSinh).getResultList();
                
                if (!res1.isEmpty()) {
                    String lg = res1.get(0).toString();
                    content.append("<p><strong>Mạng ").append(mapNguHanhToVietnamese(mang))
                           .append(" sinh tháng&nbsp;&nbsp;&nbsp;&nbsp;").append(thangSinh).append("</strong></p>");
                    content.append("<p>").append(lg.replace("\n", "<br>")).append("</p>");
                    foundContent = true;
                }
                
                // so32_co_hom_khong
                try {
                    String mangStr32 = "Mạng " + mapNguHanhToVietnamese(mang);
                    String sql2 = "SELECT So, LoiGiai FROM so32_co_hom_khong WHERE Mang = ? AND ThangSanh = ?";
                    List<?> res2 = entityManager.createNativeQuery(sql2)
                            .setParameter(1, mangStr32).setParameter(2, thangSinh).getResultList();
                    for (Object obj : res2) {
                        Object[] row = (Object[]) obj;
                        content.append("<p><strong>Số ").append(row[0]).append("</strong><br>");
                        content.append(row[1]).append("</p>");
                        foundContent = true;
                    }
                } catch (Exception e2) {
                    System.err.println("Lỗi manual Sở 32 (hòm không): " + e2.getMessage());
                }
                
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 32: " + e.getMessage());
            }
            if (foundContent) return;
        }

        // === SỞ 31: Định số sống lâu ===
        if (secNo == 31) {
            try {
                String sql = "SELECT TieuSo, DaiSo, LoiNhac FROM so31_dinh_so_song_lau WHERE MangID = ? AND ThangSanh = ?";
                List<?> res = entityManager.createNativeQuery(sql)
                        .setParameter(1, mangId).setParameter(2, thangSinh).getResultList();
                
                if (!res.isEmpty()) {
                    Object[] row = (Object[]) res.get(0);
                    int tieuSo = (int)row[0];
                    int daiSo = (int)row[1];
                    String loiNhac = row[2] != null ? row[2].toString() : "";

                    content.append("<p><strong>• Mạng ").append(mapNguHanhToVietnamese(mang)).append(" sinh tháng ").append(thangSinh).append("</strong><br>");
                    content.append("Tiểu số: ").append(tieuSo).append("<br>");
                    content.append("Đại số: ").append(daiSo).append("</p>");
                    content.append("<p><strong>• Lời Nhắc</strong><br>").append(loiNhac.replace("\n", "<br>")).append("</p>");
                    
                    foundContent = true;
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 31: " + e.getMessage());
            }
            if (foundContent) return;
        }

        // ===================== GENERIC DYNAMIC QUERY LOGIC =====================
        for (String table : tables) {
            try {
                List<Object[]> cols = entityManager.createNativeQuery("SHOW COLUMNS FROM `" + table + "`").getResultList();
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

                String thangCol = findColumn(colNames, "thangsanh", "thang_sanh", "sanh_thang", "thang_sinh", "thang_ky", "thang");
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
                    try { bYear = Integer.parseInt(request.getBirthYear()); } catch (Exception e) {}
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
                    if (!(tl.contains("tongquan") || tl.contains("loikhuyen") || tl.contains("trietly") || tl.contains("loi_ket") || tl.contains("nghile") || tl.contains("nhan_qua") || tl.contains("ngu_tang") || tl.contains("nguphuong"))) {
                        continue;
                    }
                }

                String whereClause = conditions.isEmpty() ? "1=1" : String.join(" AND ", conditions);
                List<String> outputCols = new java.util.ArrayList<>();
                for (int ci = 0; ci < colNames.size(); ci++) {
                    if (isOutputColumn(colNames.get(ci))) outputCols.add(colNamesOriginal.get(ci));
                }
                if (outputCols.isEmpty()) outputCols.add(colNamesOriginal.get(colNamesOriginal.size() - 1));

                String selectFields = "`" + String.join("`, `", outputCols) + "`";
                String sql = "SELECT " + selectFields + " FROM `" + table + "` WHERE " + whereClause + " LIMIT 20";

                try {
                    var query = entityManager.createNativeQuery(sql);
                    for (int pi = 0; pi < params.size(); pi++) query.setParameter(pi + 1, params.get(pi));
                    List<?> rawResults = query.getResultList();

                    if (!rawResults.isEmpty()) {
                        foundContent = true;
                        if (tables.size() > 1) {
                            String cleanName = table.substring(table.indexOf('_') + 1).replace("_", " ");
                            content.append("<h6 style='color:#b8860b; margin-top:10px;'>▶ ").append(cleanName.toUpperCase()).append("</h6>");
                        }
                        for (Object rawRow : rawResults) {
                            Object[] row = (rawRow instanceof Object[]) ? (Object[]) rawRow : new Object[] { rawRow };
                            StringBuilder rowVal = new StringBuilder();
                            for (int ri = 0; ri < row.length; ri++) {
                                if (row[ri] != null) {
                                    String val = row[ri].toString().replace("\n", "<br>");
                                    if (row.length > 1 && val.length() < 50 && ri < row.length - 1) {
                                        // Bỏ dấu ":" cho các Sở người dùng yêu cầu (14, 15, 16, 25, 26, 28, 29...)
                                        if (secNo == 14 || secNo == 15 || secNo == 16 || secNo == 25 || secNo == 26 || secNo == 28 || secNo == 29) {
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
                                content.append("<p style='margin-bottom:8px;'>").append(rowVal.toString()).append("</p>");
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
            if (h == 0) hStart = 23; 
            else if (hStart < 0) hStart = 23;

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
        if (chiId >= 1 && chiId <= 12) return chis[chiId];
        return String.valueOf(chiId);
    }
    
    private int calculateDayChiId(int d, int m, int y) {
        if (m < 3) {
            y--;
            m += 12;
        }
        // Công thức Julian Day đơn giản hóa để tính Chi ngày của Việt Nam
        long jd = (long)(365.25 * (y + 4716)) + (long)(30.6001 * (m + 1)) + d - 1524;
        int chi = (int)((jd + 1) % 12);
        if (chi < 0) chi += 12;
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
        // ... không cần thiết vì đã dùng inline logic ở trên, nhưng có thể giữ nếu muốn refactor
    }

    private void appendSo13TableResult(StringBuilder content, String tableName, String idColName, Object idValue,
            int thangSinh) {
        try {
            List<String> tablesFound = entityManager.createNativeQuery("SHOW TABLES LIKE '" + tableName + "'")
                    .getResultList();
            if (tablesFound.isEmpty())
                return;

            List<String> colNames = getTableColumnNames(tableName);
            if (colNames.isEmpty()) return;
            List<String> lowerCols = colNames.stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toList());

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
                String loiGiaiVal = (lgColIdx >= 0 && row[lgColIdx] != null) ? row[lgColIdx].toString().replace("\n", "<br>")
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

    private int findColIndex(java.util.List<String> colNames, String... candidates) {
        for (String candidate : candidates) {
            int idx = colNames.indexOf(candidate);
            if (idx >= 0)
                return idx;
        }
        return -1;
    }
}