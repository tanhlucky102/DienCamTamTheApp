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
        /* Sở  4 */ "Coi tuổi mạng (Ngũ hành)",
        /* Sở  5 */ "Coi Tâm giờ sanh (Ngày đêm)",
        /* Sở  6 */ "Coi Tam Thế (Tháng/Giờ/Tuổi/Mạng)",
        /* Sở  7 */ "Coi Hồn đầu thai",
        /* Sở  8 */ "Coi 36 giờ sanh",
        /* Sở  9 */ "Coi Ngày sanh",
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
        } catch (Exception e) {}

        // Chuyển đổi sang Âm lịch nếu là Dương lịch
        int birthYear, ngaySinh, thangSinh;
        String calendarNote = "";

        String calendarType = request.getCalendarType() != null ? request.getCalendarType().toLowerCase() : "";
        boolean isSolar = calendarType.contains("solar") || calendarType.contains("duong") || calendarType.contains("dương");

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

        String gioSinhFull = request.getBirthHour();
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
        content.append("<div class='log-box' style='background: #fdf6e3; padding: 15px; border-radius: 8px; border: 1px solid #eee8d5; margin-bottom: 20px; color: #657b83'>");
        content.append("<strong style='color: #b58900;'>[Thông số Diễn Cầm]</strong><br>");
        content.append("- Ngày sinh (Âm lịch): <b>").append(ngaySinh).append("/").append(thangSinh).append("/").append(birthYear).append("</b>").append(calendarNote).append("<br>");
        content.append("- Bạn tuổi: <b>").append(can.toUpperCase()).append(" ").append(chi.toUpperCase()).append("</b><br>");
        content.append("- Mạng (Ngũ Hành): <b>").append(mang.toUpperCase()).append("</b> - Cốt (Xương): <b>").append(cot.toUpperCase()).append("</b><br>");
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
                String title = (titleIndex >= 0 && titleIndex < SECTION_TITLES.length) ? SECTION_TITLES[titleIndex] : "Sở " + i;
                content.append("\n<hr><h4>Sở ").append(i).append(". ").append(title).append("</h4>\n");

                // Xử lý truy vấn động vào bảng soXX tương ứng
                processDynamicSection(content, i, request, can, chi, ngaySinh, thangSinh, canChiGio, thangThoThai, mang, cot, truongSanhId);
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
            return secNo == 31 || secNo == 32;
        }
        if (filterCategory.contains("Gia đình")) {
            return secNo == 13 || secNo == 23 || secNo == 22;
        }
        if (filterCategory.contains("Vận hạn")) {
            return secNo == 18 || secNo == 28 || secNo == 29;
        }
        if (filterCategory.contains("Bản thân")) {
            return secNo == 4 || secNo == 5 || secNo == 6 || secNo == 8 || secNo == 9 || secNo == 10 || secNo == 12 || secNo == 25 || secNo == 34;
        }
        return true;
    }

    private int lookupTruongSanhId(int chiId, int nguHanhId) {
        try {
            // Sở 21 lưu ở bảng so21_coi_truong_sanh với các cột Chi_ID, Ngu_Hanh_ID, Truong_Sanh_ID
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

    private void processDynamicSection(StringBuilder content, int secNo, DivinationRequest request,
                                       String can, String chi, int ngaySinh, int thangSinh,
                                       String canChiGio, int thangThoThai, String mang, String cot,
                                       int truongSanhId) {

        String prefix1 = String.format("so%02d", secNo);

        List<String> tables = new ArrayList<>();
        try {
            // 1. Quét tìm tất cả các bảng bắt đầu bằng "soXX" (bao gồm cả "soXX.Y_")
            List<?> rows = entityManager.createNativeQuery("SHOW TABLES LIKE '" + prefix1 + "%'").getResultList();
            for (Object row : rows) {
                tables.add(row.toString());
            }

            // Fallback: nếu user đặt tên bảng là "soX_" thay vì "so0X_" (cho secNo < 10)
            if (tables.isEmpty() && secNo < 10) {
                String prefix2 = String.format("so%d\\_", secNo);
                List<?> rows2 = entityManager.createNativeQuery("SHOW TABLES LIKE '" + prefix2 + "%'").getResultList();
                for (Object row : rows2) {
                    tables.add(row.toString());
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy bảng cho Sở " + secNo + ": " + e.getMessage());
        }

        int canId = getCanId(can);
        int chiId = getChiId(chi);
        int mangId = getNguHanhId(mang);
        String mua = getMua(thangSinh);
        String buoi = getBuoi(request.getBirthHour());

        if (tables.isEmpty()) {
            content.append("<p><em>(Nội dung cho Sở ").append(secNo).append(" đang được cập nhật trong Database...)</em></p>");
            return;
        }

        boolean foundContent = false;

        // === SỞ 5: Tâm giờ sanh ===
        if (secNo == 5) {
            try {
                String sql = "SELECT tu_gio, den_gio, ten_gio_chi FROM so05_tamgiosanh WHERE thang_sanh = ? AND (buoi = ? OR ten_gio_chi = ?) LIMIT 3";
                List<?> results = entityManager.createNativeQuery(sql)
                    .setParameter(1, thangSinh).setParameter(2, buoi).setParameter(3, canChiGio)
                    .getResultList();
                content.append("<p>Sở này dùng để xác định chính xác giờ sinh của bạn thuộc múi giờ nào trong tháng (do giờ Âm lịch thay đổi theo mùa).</p>");
                for (Object rawRow : results) {
                    Object[] r = (Object[]) rawRow;
                    content.append("<p><strong>Khung giờ ").append(r[2]).append(":</strong> từ ").append(r[0]).append(" đến ").append(r[1]).append("</p>");
                }
                foundContent = true;
            } catch (Exception e) {
                 System.err.println("Lỗi Sở 5: " + e.getMessage());
            }
            if (!foundContent) {
                content.append("<p><em>(Dữ liệu Sở 5 đang được cập nhật...)</em></p>");
            }
            return;
        }

        // === SỞ 20: Duyên nợ vợ chồng (Relational: ma_tran → loi_giai) ===
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
                    content.append("<div style='margin-bottom: 10px;'><h6 style='color:#b8860b'>▶ Duyên nợ: ").append(ketQua).append("</h6>");
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
                    content.append("<p><strong>Tháng xung khắc:</strong> ").append(xkList.get(0).toString()).append("</p>");
                    foundContent = true;
                }
            } catch (Exception e) {
                System.err.println("Lỗi Sở 20: " + e.getMessage());
            }
            if (!foundContent) {
                content.append("<p><em>(Chúc mừng bạn, theo sách Diễn Cầm Tam Thế, tuổi và tháng sinh của bạn không phạm phải tai ương này)</em></p>");
            }
            return;
        }

        // === SỞ 21: Vòng Trường Sinh ===
        if (secNo == 21) {
            try {
                if (truongSanhId > 0 && truongSanhId <= 12) {
                    String[] tsNames = {"", "Trường Sanh", "Mộc Dục", "Quan Đới", "Lâm Quan", "Đế Vượng", "Suy", "Bệnh", "Tử", "Mộ", "Tuyệt", "Thai", "Dưỡng"};
                    content.append("<p>Vòng Trường Sinh bản mệnh của bạn là: <strong>").append(tsNames[truongSanhId]).append("</strong></p>");
                    foundContent = true;
                } else {
                    content.append("<p><em>(Chưa tính được Vòng Trường Sinh vì thiếu dữ liệu tuổi/mệnh trùng khớp)</em></p>");
                    foundContent = true;
                }
            } catch (Exception e) {
                System.err.println("Lỗi Sở 21: " + e.getMessage());
            }
            return;
        }

        // === SỞ 22: Nuôi con (cần TruongSanhID) ===
        if (secNo == 22) {
            if (truongSanhId == -1) {
                content.append("<p><em>(Chưa tính được Vòng Trường Sinh - không thể tra Sở 22)</em></p>");
            } else {
                try {
                    String sql = "SELECT TenSao, loi_giai FROM so22_nuoicon WHERE (TruongSanhID = ? OR truong_sanhid = ?)";
                    List<?> results = entityManager.createNativeQuery(sql)
                        .setParameter(1, truongSanhId).setParameter(2, truongSanhId)
                        .getResultList();
                    for (Object row : results) {
                        Object[] cols = (Object[]) row;
                        content.append("<p>");
                        if (cols[0] != null) content.append("<strong>").append(cols[0].toString()).append(":</strong> ");
                        if (cols[1] != null) content.append(cols[1].toString().replace("\n", "<br>"));
                        content.append("</p>");
                        foundContent = true;
                    }

                    // Lời bàn luận chung
                    List<?> banLuan = entityManager.createNativeQuery("SELECT NoiDung FROM so22_loi_ban_luan LIMIT 5").getResultList();
                    for (Object bl : banLuan) {
                        if (bl != null) {
                            content.append("<p><em>").append(bl.toString().replace("\n", "<br>")).append("</em></p>");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi Sở 22: " + e.getMessage());
                }
            }
            if (!foundContent) {
                content.append("<p><em>(Chưa có dữ liệu khớp cho Sở 22)</em></p>");
            }
            return;
        }

        // === SỞ 23: Anh em (cần TruongSanhID) ===
        if (secNo == 23) {
            if (truongSanhId == -1) {
                content.append("<p><em>(Chưa tính được Vòng Trường Sinh - không thể tra Sở 23)</em></p>");
            } else {
                try {
                    String sql = "SELECT TenSao, BaiTho FROM so23_anhem WHERE (TruongSanhID = ? OR truong_sanhid = ?)";
                    List<?> results = entityManager.createNativeQuery(sql)
                        .setParameter(1, truongSanhId).setParameter(2, truongSanhId)
                        .getResultList();
                    for (Object row : results) {
                        Object[] cols = (Object[]) row;
                        content.append("<div style='margin-bottom: 10px;'>");
                        if (cols[0] != null) content.append("<h6 style='color:#b8860b'>▶ ").append(cols[0].toString()).append("</h6>");
                        if (cols[1] != null) content.append("<p>").append(cols[1].toString().replace("\n", "<br>")).append("</p>");
                        content.append("</div>");
                        foundContent = true;
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi Sở 23: " + e.getMessage());
                }
            }
            if (!foundContent) {
                content.append("<p><em>(Chưa có dữ liệu khớp cho Sở 23)</em></p>");
            }
            return;
        }

        // === SỞ 24: Huynh đệ (Relational: mapping → lời giải) ===
        if (secNo == 24) {
            try {
                String sql1 = "SELECT ViTri FROM so24_huynhde_mapping WHERE mua = ? AND ChiID = ?";
                List<?> vitriList = entityManager.createNativeQuery(sql1)
                    .setParameter(1, mua).setParameter(2, chiId).getResultList();
                if (!vitriList.isEmpty()) {
                    String vitri = vitriList.get(0).toString();
                    String sql2 = "SELECT BaiTho FROM so24_huynhde_loigiai WHERE ViTri = ?";
                    List<?> thoList = entityManager.createNativeQuery(sql2)
                        .setParameter(1, vitri).getResultList();
                    content.append("<div style='margin-bottom: 10px;'><h6 style='color:#b8860b'>▶ Vị trí theo mùa: ").append(vitri).append("</h6>");
                    for (Object tho : thoList) {
                        if (tho != null) {
                            content.append("<p>").append(tho.toString().replace("\n", "<br>")).append("</p>");
                        }
                    }
                    content.append("</div>");
                    foundContent = true;
                }
            } catch (Exception e) {
                System.err.println("Lỗi Sở 24: " + e.getMessage());
            }
            if (!foundContent) {
                content.append("<p><em>(Chưa có dữ liệu khớp cho Sở 24)</em></p>");
            }
            return;
        }

        // === SỞ 31: Kết luận (2-step: mang_thang → phantram → ketluan) ===
        if (secNo == 31) {
            try {
                // Bước 1: Lấy phần trăm từ Mạng + Tháng Sanh
                String sql1 = "SELECT PhanTram FROM so31_mang_thang_phantram WHERE (NguHanhID = ? OR ngu_hanhid = ?) AND (ThangSanh = ? OR thang_sanh = ?) LIMIT 1";
                List<?> ptList = entityManager.createNativeQuery(sql1)
                    .setParameter(1, mangId).setParameter(2, mangId)
                    .setParameter(3, thangSinh).setParameter(4, thangSinh)
                    .getResultList();
                if (!ptList.isEmpty() && ptList.get(0) != null) {
                    String phantram = ptList.get(0).toString();
                    content.append("<p><strong>Phần trăm bản mệnh:</strong> ").append(phantram).append("%</p>");

                    // Bước 2: Lấy kết luận dựa trên phần trăm
                    String sql2 = "SELECT KetLuan FROM so31_ketluan WHERE PhanTram = ? LIMIT 1";
                    List<?> klList = entityManager.createNativeQuery(sql2)
                        .setParameter(1, phantram).getResultList();
                    for (Object kl : klList) {
                        if (kl != null) {
                            content.append("<p>").append(kl.toString().replace("\n", "<br>")).append("</p>");
                        }
                    }
                    foundContent = true;
                }
            } catch (Exception e) {
                System.err.println("Lỗi Sở 31: " + e.getMessage());
            }
            if (!foundContent) {
                content.append("<p><em>(Chưa có dữ liệu khớp cho Sở 31)</em></p>");
            }
            return;
        }

        // === SỞ 33: Tổng luận Lục tự (Relational) ===
        if (secNo == 33) {
            try {
                String sqla = "SELECT TenKetQua FROM so33_tuoi_thang_ketqua WHERE ChiID = ? AND ThangSanh = ?";
                List<?> kqList = entityManager.createNativeQuery(sqla)
                    .setParameter(1, chiId).setParameter(2, thangSinh).getResultList();
                if (!kqList.isEmpty()) {
                    String ketQua = kqList.get(0).toString();
                    String sqlb = "SELECT LoiGiai FROM so33_loigiai_chiet WHERE KetQuaChinh = ?";
                    List<?> lgList = entityManager.createNativeQuery(sqlb)
                        .setParameter(1, ketQua).getResultList();
                    content.append("<div style='margin-bottom: 10px;'><h6 style='color:#b8860b'>▶ Chữ Lục tự: ").append(ketQua).append("</h6>");
                    for (Object lg : lgList) {
                        if (lg != null) {
                            content.append("<p>").append(lg.toString().replace("\n", "<br>")).append("</p>");
                        }
                    }
                    content.append("</div>");
                    foundContent = true;
                }
            } catch (Exception e) {
                System.err.println("Lỗi Sở 33: " + e.getMessage());
            }
            if (!foundContent) {
                content.append("<p><em>(Chưa có dữ liệu khớp cho Sở 33)</em></p>");
            }
            return;
        }

        // ===================== GENERIC DYNAMIC QUERY LOGIC =====================
        // Áp dụng cho các Sở còn lại dùng truy vấn tự động dựa trên tên cột

        for (String table : tables) {
            try {
                // Lấy danh sách cột, normalize sang lowercase
                List<Object[]> cols = entityManager.createNativeQuery("SHOW COLUMNS FROM `" + table + "`").getResultList();
                List<String> colNames = new java.util.ArrayList<>();
                List<String> colNamesOriginal = new java.util.ArrayList<>();
                for (Object[] colObj : cols) {
                    colNames.add(colObj[0].toString().toLowerCase());
                    colNamesOriginal.add(colObj[0].toString());
                }

                List<String> conditions = new java.util.ArrayList<>();
                List<Object> params = new java.util.ArrayList<>();

                // === Khớp cột CanID ===
                String canCol = findColumn(colNames, "canid", "can_id", "tuoi_canid");
                if (canCol != null) {
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, canCol) + "` = ?");
                    params.add(canId);
                }

                // === Khớp cột ChiID ===
                // Sở 8 và Sở 5: dùng ChiID của giờ sinh, không phải tuổi
                String chiCol = findColumn(colNames, "chiid", "chi_id", "tuoi_chiid");
                if (chiCol != null) {
                    int chiVal = (secNo == 8 || secNo == 5) ? getChiId(canChiGio) : chiId;
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, chiCol) + "` = ?");
                    params.add(chiVal);
                }

                // === Cột tuoi_nam (tên Chi dạng text) ===
                if (chiCol == null && colNames.contains("tuoi_nam")) {
                    conditions.add("`tuoi_nam` = ?");
                    params.add(chi);
                }

                // === Khớp cột NguHanhID / Mạng ===
                String mangCol = findColumn(colNames, "nguhanhid", "ngu_hanhid", "mang_id");
                if (mangCol != null) {
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, mangCol) + "` = ?");
                    params.add(mangId);
                } else if (colNames.contains("mang")) {
                    conditions.add("(`Mang` = ? OR `Mang` = ?)");
                    params.add(mangId);
                    params.add(mapNguHanhToVietnamese(mang));
                }

                // === Khớp cột ThangSanh ===
                String thangCol = findColumn(colNames, "thangsanh", "thang_sanh", "sanh_thang", "thang_sinh", "thang_ky", "thang");
                if (thangCol != null) {
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, thangCol) + "` = ?");
                    params.add(thangSinh);
                }

                // === Khớp cột NgaySanh ===
                String ngayCol = findColumn(colNames, "ngaysanh", "ngay");
                if (ngayCol != null) {
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, ngayCol) + "` = ?");
                    params.add(ngaySinh);
                }

                // === Khớp cột GioID ===
                String gioCol = findColumn(colNames, "gioid", "gio_id", "gio_ky", "ten_gio_chi");
                if (gioCol != null) {
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, gioCol) + "` = ?");
                    if (gioCol.equals("gio_ky") || gioCol.equals("ten_gio_chi")) {
                        params.add(canChiGio);
                    } else {
                        params.add(getChiId(canChiGio));
                    }
                }

                // === Khớp cột Thọ Thai Tháng ===
                if (colNames.contains("tho_thai_thang")) {
                    conditions.add("`tho_thai_thang` = ?");
                    params.add(thangThoThai);
                }

                // === Khớp cột Buổi (TRƯỚC khi check empty!) ===
                if (colNames.contains("buoi")) {
                    conditions.add("`buoi` = ?");
                    params.add(buoi);
                }

                // === Khớp cột Mùa ===
                String muaCol = findColumn(colNames, "muaid", "mua_id", "mua");
                if (muaCol != null) {
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, muaCol) + "` = ?");
                    params.add(mua);
                }

                // === Giới Tính ===
                String genderReq = request.getGender();
                String genderCol = findColumn(colNames, "gioitinh", "gioi_tinh");
                if (genderReq != null && genderCol != null) {
                    String genderVn = getGenderVn(genderReq);
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, genderCol) + "` = ?");
                    params.add(genderVn);
                }

                // === Tuổi Âm Lịch ===
                String tuoiCol = findColumn(colNames, "tuoiamlich", "tuoi_am_lich");
                if (tuoiCol != null) {
                    int cYear = java.time.Year.now().getValue();
                    int bYear = 1999;
                    try { bYear = Integer.parseInt(request.getBirthYear()); } catch (Exception e){}
                    int tuoiAmLich = cYear - bYear + 1;
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, tuoiCol) + "` = ?");
                    params.add(tuoiAmLich);
                }

                // === TruongSanhID — cần lookup riêng, KHÔNG skip ===
                String tsCol = findColumn(colNames, "truongsanhid", "truong_sanhid");
                if (tsCol != null) {
                    if (truongSanhId == -1) {
                        content.append("<p><em>(Chưa tính được Vòng Trường Sinh cho bảng ").append(table).append(")</em></p>");
                        continue;
                    }
                    conditions.add("`" + getOriginalCol(colNamesOriginal, colNames, tsCol) + "` = ?");
                    params.add(truongSanhId);
                }

                // === Cột so_muc / somuc ===
                // Ghi chú: Cột so_muc trong DB có thể đánh số theo kiểu cũ (VD: Sở 6 có so_muc=13).
                // DO ĐÓ: Không được filter trực tiếp `so_muc = secNo` nữa, sẽ làm mất dữ liệu!
                // if (colNames.contains("so_muc")) {
                //     conditions.add("`so_muc` = ?");
                //     params.add(secNo);
                // } else if (colNames.contains("somuc")) {
                //     conditions.add("`somuc` = ?");
                //     params.add(secNo);
                // }

                // === Tránh dump toàn bảng: nếu KHÔNG CÓ condition nào ===
                if (conditions.isEmpty()) {
                    // Chỉ cho phép bảng tĩnh (tổng quan, lời khuyên, triết lý, lời kết, nghi lễ, nhân quả, ngũ tạng, ngũ phương)
                    String tl = table.toLowerCase();
                    if (tl.contains("tongquan") || tl.contains("loikhuyen") || tl.contains("trietly")
                        || tl.contains("loi_ket") || tl.contains("nghile") || tl.contains("nhan_qua")
                        || tl.contains("ngu_tang") || tl.contains("nguphuong")) {
                        // OK - cho phép lấy toàn bộ
                    } else {
                        continue; // Không có condition mà bảng thuộc loại data lớn -> Bỏ qua
                    }
                }

                String whereClause = conditions.isEmpty() ? "1=1" : String.join(" AND ", conditions);

                // Lọc ra CÁC cột chứa text/nội dung để hiển thị (bỏ qua cột filter/ID)
                List<String> outputCols = new java.util.ArrayList<>();
                for (int ci = 0; ci < colNames.size(); ci++) {
                    String c = colNames.get(ci);
                    if (isOutputColumn(c)) {
                        outputCols.add(colNamesOriginal.get(ci));
                    }
                }
                if (outputCols.isEmpty()) {
                    // Fallback: lấy cột cuối cùng
                    outputCols.add(colNamesOriginal.get(colNamesOriginal.size() - 1));
                }

                String selectFields = "`" + String.join("`, `", outputCols) + "`";
                String sql = "SELECT " + selectFields + " FROM `" + table + "` WHERE " + whereClause + " LIMIT 20";

                var query = entityManager.createNativeQuery(sql);
                for (int pi = 0; pi < params.size(); pi++) {
                    query.setParameter(pi + 1, params.get(pi));
                }

                List<?> rawResults = query.getResultList();

                if (!rawResults.isEmpty()) {
                    foundContent = true;
                    if (tables.size() > 1) {
                        String cleanName = table.substring(table.indexOf('_') + 1).replace("_", " ");
                        content.append("<h6 style='color:#b8860b; margin-top:10px;'>▶ ").append(cleanName.toUpperCase()).append("</h6>");
                    }

                    for (Object rawRow : rawResults) {
                        Object[] row;
                        if (rawRow instanceof Object[]) {
                            row = (Object[]) rawRow;
                        } else {
                            row = new Object[]{rawRow};
                        }

                        StringBuilder rowVal = new StringBuilder();
                        for (int ri = 0; ri < row.length; ri++) {
                            if (row[ri] != null) {
                                String val = row[ri].toString().replace("\n", "<br>");
                                // Nếu cột ngắn (tên/label), in đậm; nếu dài (lời giải), in thường
                                if (row.length > 1 && val.length() < 50 && ri < row.length - 1) {
                                    rowVal.append("<strong>").append(val).append(":</strong> ");
                                } else {
                                    rowVal.append(val).append("<br>");
                                }
                            }
                        }
                        if (rowVal.length() > 0) {
                            content.append("<p>").append(rowVal.toString()).append("</p>");
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println("Lỗi query động bảng " + table + ": " + e.getMessage());
            }
        }

        if (!foundContent) {
            if (secNo == 18 || secNo == 19 || secNo == 20 || secNo == 27) {
                content.append("<p><em>(Chúc mừng bạn, theo sách Diễn Cầm Tam Thế, tuổi và tháng sinh của bạn không phạm phải tai ương này)</em></p>");
            } else {
                content.append("<p><em>(Sở này đã có bảng nhưng chưa có dữ liệu trùng khớp với thông tin của bạn)</em></p>");
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
        if (colLower.equals("id")) return false;
        if (colLower.equals("so") || colLower.equals("so_muc") || colLower.equals("somuc")) return false;
        if (colLower.equals("created_at")) return false;

        // Loại các cột dùng để filter (ID columns)
        if (colLower.equals("canid") || colLower.equals("can_id") || colLower.equals("tuoi_canid")) return false;
        if (colLower.equals("chiid") || colLower.equals("chi_id") || colLower.equals("tuoi_chiid")) return false;
        if (colLower.equals("nguhanhid") || colLower.equals("ngu_hanhid") || colLower.equals("mang_id")) return false;
        if (colLower.equals("truongsanhid") || colLower.equals("truong_sanhid")) return false;
        if (colLower.equals("muaid") || colLower.equals("mua_id")) return false;
        if (colLower.equals("gioid") || colLower.equals("gio_id") || colLower.equals("ten_gio_chi")) return false;

        // Loại các cột đầu vào filter
        if (colLower.equals("thangsanh") || colLower.equals("thang_sanh") || colLower.equals("sanh_thang")) return false;
        if (colLower.equals("thang_sinh") || colLower.equals("thang_ky") || colLower.equals("thang")) return false;
        if (colLower.equals("ngaysanh") || colLower.equals("ngay")) return false;
        if (colLower.equals("gio_ky")) return false;
        if (colLower.equals("gioitinh") || colLower.equals("gioi_tinh")) return false;
        if (colLower.equals("tuoiamlich") || colLower.equals("tuoi_am_lich")) return false;
        if (colLower.equals("tuoi_nam")) return false;
        if (colLower.equals("buoi")) return false;
        if (colLower.equals("mua")) return false;
        if (colLower.equals("tho_thai_thang")) return false;

        return true;
    }

    private String getGenderVn(String genderReq) {
        if (genderReq == null) return "Nam";
        return (genderReq.toLowerCase().contains("fe") || genderReq.toLowerCase().contains("nu") || genderReq.toLowerCase().contains("nữ")) ? "Nữ" : "Nam";
    }

    private String getMua(int thangSinh) {
        if (thangSinh >= 1 && thangSinh <= 3) return "Xuân";
        if (thangSinh >= 4 && thangSinh <= 6) return "Hạ";
        if (thangSinh >= 7 && thangSinh <= 9) return "Thu";
        return "Đông";
    }

    private String getBuoi(String gioSinh) {
        if (gioSinh == null || gioSinh.isEmpty()) return "Ngay";
        try {
            // Trích xuất giờ từ chuỗi input
            String hourStr = gioSinh.trim();
            if (hourStr.contains(":")) {
                hourStr = hourStr.substring(0, hourStr.indexOf(":")).trim();
            }
            int hour = Integer.parseInt(hourStr);
            // Đêm: 17h-04h (Dậu, Tuất, Hợi, Tý, Sửu, Dần)
            if (hour >= 17 || hour <= 4) return "Dem";
        } catch (Exception e) {}
        return "Ngay";
    }

    private int getCanId(String can) {
        String[] cans = {"giáp", "ất", "bính", "đinh", "mậu", "kỷ", "canh", "tân", "nhâm", "quý"};
        String lower = can.toLowerCase();
        for (int i = 0; i < cans.length; i++) {
            if (lower.contains(cans[i])) return i + 1;
        }
        return 1;
    }

    private int getChiId(String chi) {
        String[] chis = {"tý", "sửu", "dần", "mẹo", "thìn", "tỵ", "ngọ", "mùi", "thân", "dậu", "tuất", "hợi"};
        String lower = chi.toLowerCase();
        for (int i = 0; i < chis.length; i++) {
            if (lower.contains(chis[i]) || (lower.contains("mão") && i == 3)) return i + 1;
        }
        return 1;
    }

    private int getNguHanhId(String mang) {
        String lower = mang.toLowerCase();
        if (lower.contains("kim")) return 1;
        if (lower.contains("mộc") || lower.contains("moc")) return 2;
        if (lower.contains("thủy") || lower.contains("thuy")) return 3;
        if (lower.contains("hỏa") || lower.contains("hoa")) return 4;
        if (lower.contains("thổ") || lower.contains("tho")) return 5;
        return 1;
    }

    private String mapNguHanhToVietnamese(String mang) {
        String lower = mang.toLowerCase();
        if (lower.contains("kim")) return "Kim";
        if (lower.contains("thuy") || lower.contains("thủy")) return "Thủy";
        if (lower.contains("hoa") || lower.contains("hỏa")) return "Hỏa";
        if (lower.contains("tho") || lower.contains("thổ")) return "Thổ";
        if (lower.contains("moc") || lower.contains("mộc")) return "Mộc";
        return mang;
    }

    private String extractCanChi(String rawGio) {
        if (rawGio == null || rawGio.isEmpty()) return "Ngọ";
        if (rawGio.contains(":") && rawGio.length() >= 5) {
            try {
                int h = Integer.parseInt(rawGio.substring(0, 2).trim());
                if (h >= 23 || h < 1) return "Tý";
                if (h >= 1 && h < 3) return "Sửu";
                if (h >= 3 && h < 5) return "Dần";
                if (h >= 5 && h < 7) return "Mẹo";
                if (h >= 7 && h < 9) return "Thìn";
                if (h >= 9 && h < 11) return "Tỵ";
                if (h >= 11 && h < 13) return "Ngọ";
                if (h >= 13 && h < 15) return "Mùi";
                if (h >= 15 && h < 17) return "Thân";
                if (h >= 17 && h < 19) return "Dậu";
                if (h >= 19 && h < 21) return "Tuất";
                if (h >= 21 && h < 23) return "Hợi";
            } catch (Exception e) {}
        }
        // Fallback: parse từ chuỗi Chi trực tiếp
        int idx = rawGio.indexOf(" ");
        return idx > 0 ? rawGio.substring(0, idx).trim() : rawGio;
    }

    private String mapChiToCot(String chi) {
        if (chi == null) return "chuot";
        String lower = chi.toLowerCase();
        if (lower.contains("tý") || lower.contains("ty1")) return "chuot";
        if (lower.contains("sửu") || lower.contains("suu")) return "trau";
        if (lower.contains("dần") || lower.contains("dan")) return "cop";
        if (lower.contains("mẹo") || lower.contains("mão") || lower.contains("mao")) return "tho";
        if (lower.contains("thìn") || lower.contains("thin")) return "rong";
        if (lower.contains("tỵ") || lower.contains("ty2")) return "ran";
        if (lower.contains("ngọ") || lower.contains("ngo")) return "ngua";
        if (lower.contains("mùi") || lower.contains("mui")) return "de";
        if (lower.contains("thân") || lower.contains("than")) return "khi";
        if (lower.contains("dậu") || lower.contains("dau")) return "ga";
        if (lower.contains("tuất") || lower.contains("tuat")) return "cho";
        if (lower.contains("hợi") || lower.contains("hoi")) return "heo";
        return "chuot";
    }

    private int getCanIndex(String can) {
        String[] cans = {"canh", "tân", "nhâm", "quý", "giáp", "ất", "bính", "đinh", "mậu", "kỷ"};
        String[] cansAscii = {"canh", "tan", "nham", "quy", "giap", "at", "binh", "dinh", "mau", "ky"};
        for (int i = 0; i < cans.length; i++) {
            if (cans[i].equalsIgnoreCase(can) || cansAscii[i].equalsIgnoreCase(can)) return i;
        }
        return 0;
    }

    private int getChiIndex(String chi) {
        String[] chis = {"thân", "dậu", "tuất", "hợi", "tý", "sửu", "dần", "mẹo", "thìn", "tỵ", "ngọ", "mùi"};
        String[] chisAscii = {"than", "dau", "tuat", "hoi", "ty1", "suu", "dan", "mao", "thin", "ty2", "ngo", "mui"};
        for (int i = 0; i < chis.length; i++) {
            if (chis[i].equalsIgnoreCase(chi) || chisAscii[i].equalsIgnoreCase(chi)) return i;
        }
        return 0;
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
                return "kim";
            case 2:
                return "thuy";
            case 3:
                return "hoa";
            case 4:
                return "tho";
            case 5:
                return "moc";
            default:
                return "kim";
        }
    }
}