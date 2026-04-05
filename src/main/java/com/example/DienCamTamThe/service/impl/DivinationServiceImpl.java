package com.example.DienCamTamThe.service.impl;

import com.example.DienCamTamThe.entity.*;
import com.example.DienCamTamThe.repository.*;
import com.example.DienCamTamThe.util.LunarCalendarUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class DivinationServiceImpl {

    @Autowired
    private BookSectionRepository sectionRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public Map<String, String> processDivination(DivinationRequest request) {
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
        
        boolean isSolar = true; // Bắt buộc nhập ngày Dương như yêu cầu
        
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

        String can = LunarCalendarUtil.getCan(birthYear).toLowerCase();
        String chi = LunarCalendarUtil.getChi(birthYear).toLowerCase();
        String cot = mapChiToCot(chi);
        String napAmFull = LunarCalendarUtil.getNapAm(birthYear);
        String canXuong = LunarCalendarUtil.getCanXuong(birthYear, thangSinh, ngaySinh, gioSinhFull);
        
        String jsonFormat = "{"
            + "\"duong_lich\":\"" + String.format("%02d/%02d/%d", rawDay, rawMonth, rawYear) + "\","
            + "\"am_lich\":\"" + String.format("%02d/%02d/%d", ngaySinh, thangSinh, birthYear) + "\","
            + "\"can_chi\":\"" + LunarCalendarUtil.getCanChiString(birthYear) + "\","
            + "\"nap_am\":\"" + napAmFull + "\","
            + "\"can_xuong\":\"" + canXuong + "\","
            + "\"ghi_chu\":\"Tích hợp hệ Diễn Cầm và Cân Xương\""
            + "}";

        String fullname = request.getFullname() != null && !request.getFullname().isEmpty() ? request.getFullname().toUpperCase() : "BẠN";
        String displayGio = canChiGio != null && canChiGio.length() > 0 ? canChiGio.substring(0, 1).toUpperCase() + canChiGio.substring(1).toLowerCase() : "Không rõ";

        content.append("<div class='tuvi-card' style='background: linear-gradient(145deg, #ffffff, #f5f7fa); border-radius: 12px; border: 1px solid #e1e8ed; box-shadow: 0 4px 15px rgba(0,0,0,0.05); padding: 20px; margin-bottom: 25px; font-family: sans-serif; color: #334155;'>");
        content.append("  <div style='text-align: center; margin-bottom: 15px; border-bottom: 2px dashed #cbd5e1; padding-bottom: 10px;'>");
        content.append("    <h3 style='margin: 0; color: #b45309; text-transform: uppercase; letter-spacing: 1px;'>THÔNG TIN BẢN MỆNH</h3>");
        content.append("    <p style='margin: 5px 0 0 0; font-size: 16px; font-weight: bold; color: #0f172a;'>").append(fullname).append("</p>");
        content.append("  </div>");
        content.append("  <table style='width: 100%; border-collapse: collapse; font-size: 15px;'>");
        
        // Row 1: Dương lịch & Âm lịch
        content.append("    <tr>");
        content.append("      <td style='padding: 8px 0; border-bottom: 1px solid #f1f5f9;'><span style='color: #64748b;'>Dương lịch:</span> <strong style='color: #1e293b;'>").append(String.format("%02d/%02d/%d", rawDay, rawMonth, rawYear)).append("</strong></td>");
        content.append("      <td style='padding: 8px 0; border-bottom: 1px solid #f1f5f9;'><span style='color: #64748b;'>Âm lịch:</span> <strong style='color: #1e293b;'>").append(String.format("%02d/%02d/%d", ngaySinh, thangSinh, birthYear)).append("</strong></td>");
        content.append("    </tr>");
        
        // Row 2: Năm Can Chi & Giờ sinh
        content.append("    <tr>");
        content.append("      <td style='padding: 8px 0; border-bottom: 1px solid #f1f5f9;'><span style='color: #64748b;'>Năm sinh:</span> <strong style='color: #cf2a27;'>").append(can.toUpperCase()).append(" ").append(chi.toUpperCase()).append("</strong></td>");
        content.append("      <td style='padding: 8px 0; border-bottom: 1px solid #f1f5f9;'><span style='color: #64748b;'>Giờ sinh:</span> <strong style='color: #1e293b;'>").append(displayGio).append("</strong></td>");
        content.append("    </tr>");
        
        // Row 3: Nạp Âm & Cốt Xương
        content.append("    <tr>");
        content.append("      <td style='padding: 8px 0; border-bottom: 1px solid #f1f5f9;'><span style='color: #64748b;'>Ngũ Hành:</span> <strong style='color: #047857;'>").append(napAmFull).append("</strong></td>");
        content.append("      <td style='padding: 8px 0; border-bottom: 1px solid #f1f5f9;'><span style='color: #64748b;'>Hóa cốt:</span> <strong style='color: #1e293b;'>Con ").append(cot.substring(0, 1).toUpperCase() + cot.substring(1)).append("</strong></td>");
        content.append("    </tr>");

        // Row 4: Cân xương tính số & Chuyên mục tra cứu
        content.append("    <tr>");
        content.append("      <td style='padding: 8px 0;'><span style='color: #64748b;'>Cân xương lượng số:</span> <strong style='color: #b45309;'>").append(canXuong).append("</strong></td>");
        content.append("      <td style='padding: 8px 0;'><span style='color: #64748b;'>Lĩnh vực tra cứu:</span> <strong style='color: #4338ca;'>").append(category).append("</strong></td>");
        content.append("    </tr>");
        
        content.append("  </table>");
        
        if (!calendarNote.isEmpty()) {
             content.append("  <div style='margin-top: 10px; font-size: 13px; color: #94a3b8; font-style: italic; text-align: center;'>* Đã dùng lịch Mặt Trăng quy đổi từ dương lịch</div>");
        }
        content.append("</div>");

        int canId = getDatabaseCanId(birthYear);
        int chiId = getDatabaseChiId(birthYear);
        int nguHanhId = 1; // Default
        String menhChiTiet = "";
        try {
            List<Map<String, Object>> mRes = jdbcTemplate.queryForList("SELECT NguHanhID, ChiTietMang FROM so04_tuoimang WHERE CanID = ? AND ChiID = ?", canId, chiId);
            if (!mRes.isEmpty()) {
                nguHanhId = (Integer) mRes.get(0).get("NguHanhID");
                menhChiTiet = (String) mRes.get(0).get("ChiTietMang");
            }
        } catch(Exception e){}

        if(napAmFull == null || napAmFull.isEmpty()) napAmFull = menhChiTiet;

        List<BookSection> sections = sectionRepo.findAllByOrderBySectionNoAsc();
        boolean foundAny = false;

        for (BookSection sec : sections) {
            if (!isSectionInCategory(sec, category))
                continue;

            foundAny = true;
            content.append("<hr><h4>Sở ").append(sec.getSectionNo()).append(". ").append(sec.getTitle())
                    .append("</h4>");

            if (sec.getSectionNo() == 4) {
                 appendSo04(content, canId, chiId);
            } else if (sec.getSectionNo() == 7) {
                 appendSo07(content, nguHanhId, thangSinh);
            } else if (sec.getSectionNo() == 8) {
                 appendSo08(content, gioSinhFull, request.getBirthMinute(), canChiGio);
            } else if (sec.getSectionNo() == 9) {
                 appendSo09(content, ngaySinh);
            } else if (sec.getSectionNo() == 11) {
                 appendSo11(content, nguHanhId, thangSinh);
            } else if (sec.getSectionNo() == 12) {
                 appendSo12(content, chiId, thangSinh);
            } else if (sec.getSectionNo() == 13) {
                 appendSo13(content, canId, chiId, thangSinh, sec.getTitle());
            } else if (sec.getSectionNo() == 14) {
                 appendSo14(content, chiId, thangSinh);
            } else if (sec.getSectionNo() == 15) {
                 appendSo15(content, nguHanhId, thangSinh);
            } else if (sec.getSectionNo() == 16) {
                 appendSo16(content, chiId, thangSinh);
            } else if (sec.getSectionNo() == 17) {
                 appendSo17(content, chiId, thangSinh);
            } else if (sec.getSectionNo() == 18) {
                 appendSo18(content, canId, chiId, thangSinh);
            } else if (sec.getSectionNo() == 19) {
                 appendSo19(content, chiId, thangSinh, request.getGender());
            } else if (sec.getSectionNo() == 20) {
                 appendSo20(content, request, canId, chiId, thangSinh);
            } else if (sec.getSectionNo() == 21) {
                 appendSo21(content, nguHanhId, chiId);
            } else if (sec.getSectionNo() == 22) {
                 appendSo22(content, nguHanhId, chiId);
            } else if (sec.getSectionNo() == 23) {
                 appendSo23(content, nguHanhId, chiId);
            } else if (sec.getSectionNo() == 24) {
                 appendSo24(content, chiId, thangSinh);
            } else if (sec.getSectionNo() == 25) {
                 appendSo25(content, chiId, thangSinh);
            } else if (sec.getSectionNo() == 26) {
                 appendSo26(content, chiId, thangSinh);
            } else if (sec.getSectionNo() == 27) {
                 appendSo27(content, canId, chiId, request.getGender());
            } else if (sec.getSectionNo() == 28) {
                 appendSo28(content, request.getGender(), birthYear);
            } else if (sec.getSectionNo() == 29) {
                 appendSo29(content, request.getGender(), birthYear);
            } else if (sec.getSectionNo() == 30) {
                 appendSo30(content, chiId, thangSinh);
            } else if (sec.getSectionNo() == 31) {
                 appendSo31(content, nguHanhId, thangSinh);
            } else if (sec.getSectionNo() == 32) {
                 appendSo32(content, chiId, thangSinh);
            } else if (sec.getSectionNo() == 34) {
                 appendSo34(content, chiId, thangSinh);
            } else {
                content.append("<p><em>(Dữ liệu Sở ").append(sec.getSectionNo()).append(" đang được phân tích và cập nhật DB...)</em></p>");
            }
        }

        if (!foundAny) {
            content.append("<p>Không có dữ liệu chi tiết cho hạng mục này.</p>");
        }

        content.append("</div>");
        
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("html", content.toString());
        resultMap.put("json", jsonFormat);
        return resultMap;
    }

    private boolean isSectionInCategory(BookSection sec, String filterCategory) {
        int secNo = sec.getSectionNo();
        String dbCat = sec.getCategory() != null ? sec.getCategory() : "";

        if (filterCategory.equals("Tất cả")) {
            return true;
        }
        if (filterCategory.contains("Tình cảm")) {
            return dbCat.equals("hon_nhan") || dbCat.equals("gia_dao") || secNo == 19 || secNo == 20 || secNo == 22
                    || secNo == 23;
        }
        if (filterCategory.contains("Công danh")) {
            return dbCat.equals("nghe_nghiep") || dbCat.equals("thi_cu") || dbCat.equals("hoc_hanh") || secNo == 11
                    || secNo == 13 || secNo == 16 || secNo == 17;
        }
        if (filterCategory.contains("Tài lộc")) {
            return dbCat.equals("nha_dat") || dbCat.equals("khac") || secNo == 15 || secNo == 24 || secNo == 26;
        }
        if (filterCategory.contains("Sức khỏe")) {
            return dbCat.equals("cung_kien") || secNo == 31 || secNo == 32;
        }
        if (filterCategory.contains("Gia đình")) {
            return dbCat.equals("anh_em") || dbCat.equals("con_cai") || secNo == 23;
        }
        if (filterCategory.contains("Vận hạn")) {
            return dbCat.equals("van_menh") || secNo == 18 || secNo == 28 || secNo == 29;
        }
        if (filterCategory.contains("Bản thân")) {
            return dbCat.equals("van_menh") || secNo == 8 || secNo == 9 || secNo == 10 || secNo == 12 || secNo == 25
                    || secNo == 34;
        }
        return true;
    }

    // --- SỞ 04: Mạng ---
    private void appendSo04(StringBuilder content, int canId, int chiId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT ChiTietMang FROM so04_tuoimang WHERE CanID = ? AND ChiID = ?", canId, chiId);
            if (!rows.isEmpty()) {
                String mang = (String) rows.get(0).get("ChiTietMang");
                content.append("<p><strong>Ngũ hành nạp âm:</strong> ").append(mang).append("</p>");
            } else {
                content.append("<p><em>(Không tìm thấy dữ liệu)</em></p>");
            }
        } catch (Exception e) {
            content.append("<p><em>(Lỗi tra cứu bảng so04_tuoimang)</em></p>");
        }
    }

    // --- SỞ 07: Hồn thác ---
    private void appendSo07(StringBuilder content, int nguHanhId, int thangSinh) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT LoiGiai FROM so07_hondauthai WHERE NguHanhID = ? AND ThangSanh = ?", nguHanhId, thangSinh);
            if (!rows.isEmpty()) {
                String lg = (String) rows.get(0).get("LoiGiai");
                content.append("<p>").append(lg.replace("\n", "<br>")).append("</p>");
            } else {
                content.append("<p><em>(Không tìm thấy dữ liệu hồn thác)</em></p>");
            }
        } catch (Exception e) {
            content.append("<p><em>(Lỗi tra cứu)</em></p>");
        }
    }

    // --- SỞ 08: Giờ sinh tam thế ---
    private void appendSo08(StringBuilder content, String hourInput, String minuteInput, String canChiGio) {
        int chiGioId = getDatabaseChiIdForHour(hourInput);
        String giaiDoan = "Giữa";
        try {
            if (minuteInput != null && !minuteInput.isEmpty()) {
                int min = Integer.parseInt(minuteInput);
                if (min < 40) giaiDoan = "Đầu";
                else if (min < 80) giaiDoan = "Giữa";
                else giaiDoan = "Sau";
            }
        } catch (Exception e) {}

        content.append("<p><strong>Giờ sinh:</strong> ").append(canChiGio).append(" (Giai đoạn: ").append(giaiDoan).append(")</p>");
        
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT LoiGiai FROM so08_36giosanh WHERE ChiID = ? AND GiaiDoan = ?", chiGioId, giaiDoan);
            if (!rows.isEmpty()) {
                String lg = (String) rows.get(0).get("LoiGiai");
                content.append("<p>").append(lg != null ? lg.replace("\n", "<br>") : "").append("</p>");
            } else {
                content.append("<p><em>(Dữ liệu giờ ").append(canChiGio).append(" giai đoạn ").append(giaiDoan).append(" chưa được cập nhật trong Database. Xin đợi bản vá tiếp theo!)</em></p>");
            }
        } catch (Exception e) {
            content.append("<p><em>(Lỗi tra cứu bảng so08_36giosanh)</em></p>");
        }
    }

    // --- SỞ 09: Hiệu ngày ---
    private void appendSo09(StringBuilder content, int ngaySinh) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT TenSao, LoiGiai FROM so09_ngaysanh WHERE Ngay = ?", ngaySinh);
            if (!rows.isEmpty()) {
                String tenSao = (String) rows.get(0).get("TenSao");
                String lg = (String) rows.get(0).get("LoiGiai");
                content.append("<p><strong>Sao chiếu ngày:</strong> ").append(tenSao).append("</p>");
                content.append("<p>").append(lg.replace("\n", "<br>")).append("</p>");
            } else {
                content.append("<p><em>(Không tìm thấy dữ liệu cho ngày ").append(ngaySinh).append(")</em></p>");
            }
        } catch (Exception e) {
            content.append("<p><em>(Lỗi tra cứu bảng so09_ngaysanh)</em></p>");
        }
    }

    // --- SỞ 11: Làm ăn nghề nghiệp gì ---
    private void appendSo11(StringBuilder content, int nguHanhId, int thangSinh) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT NhomNghe FROM so11_nghenghiep WHERE NguHanhID = ? AND ThangSanh = ?", nguHanhId, thangSinh);
            if (!rows.isEmpty()) {
                String lg = (String) rows.get(0).get("NhomNghe");
                content.append("<p><strong>Nhóm nghề khuyên làm:</strong> ").append(lg != null ? lg.replace("\n", "<br>") : "").append("</p>");
            } else {
                content.append("<p><em>(Không có dữ liệu nghề nghiệp)</em></p>");
            }
        } catch (Exception e) {
            content.append("<p><em>(Lỗi tra cứu bảng so11)</em></p>");
        }
    }

    // --- SỞ 12: Cốt con gì ---
    private void appendSo12(StringBuilder content, int chiId, int thangSinh) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT TenCot, LoiGiai FROM so12_cotcongi WHERE Tuoi_ChiID = ? AND ThangSanh = ?", chiId, thangSinh);
            if (!rows.isEmpty()) {
                String tenCot = (String) rows.get(0).get("TenCot");
                String lg = (String) rows.get(0).get("LoiGiai");
                content.append("<p><strong>Cốt:</strong> Con ").append(tenCot).append("</p>");
                content.append("<p>").append(lg != null ? lg.replace("\n", "<br>") : "").append("</p>");
            } else {
                content.append("<p><em>(Chưa có dữ liệu cốt cho tổ hợp này)</em></p>");
            }
        } catch (Exception e) {
            content.append("<p><em>(Lỗi tra cứu bảng so12_cotcongi)</em></p>");
        }
    }

    // --- SỞ 13: Các bảng sao/Lộc/Thú/Sát ---
    private void appendSo13(StringBuilder content, int canId, int chiId, int thangSinh, String title) {
        try {
            if (title.contains("13d") || title.toLowerCase().contains("12 lộc")) {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT TenKetQua FROM so13_can_loc WHERE CanID = ? AND ThangSanh = ?", canId, thangSinh);
                if (!rows.isEmpty()) content.append("<p><strong>Kết quả:</strong> ").append(rows.get(0).get("TenKetQua")).append("</p>");
            }
            else if (title.contains("13e") || title.toLowerCase().contains("chi bảng 1")) {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT KetQua, LoiGiai FROM so13_chi_thu WHERE ChiID = ? AND ThangSanh = ?", chiId, thangSinh);
                if (!rows.isEmpty()) {
                    content.append("<p><strong>Sao/Thú:</strong> ").append(rows.get(0).get("KetQua")).append("</p>");
                    String lg = (String) rows.get(0).get("LoiGiai");
                    if (lg != null) content.append("<p>").append(lg.replace("\n", "<br>")).append("</p>");
                }
            } else {
               content.append("<p><em>(Chờ update truy vấn riêng cho nhánh ").append(title).append(")</em></p>");
            }
        } catch (Exception e) {
            content.append("<p><em>(Lỗi tra cứu Sở 13)</em></p>");
        }
    }

    // --- SỞ 14: Nuôi thú vật ---
    private void appendSo14(StringBuilder content, int chiId, int thangSinh) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT KetQua, LoiGiai FROM so14_nuoithuvat WHERE ChiID = ? AND ThangSanh = ?", chiId, thangSinh);
            if (!rows.isEmpty()) {
                String lg = (String) rows.get(0).get("LoiGiai");
                content.append("<p><strong>Đoán:</strong> ").append(rows.get(0).get("KetQua")).append("</p>");
                content.append("<p>").append(lg != null ? lg.replace("\n", "<br>") : "").append("</p>");
            } else { content.append("<p><em>(Chưa có dữ liệu)</em></p>"); }
        } catch (Exception e) {}
    }

    // --- SỞ 15: Ruộng đất ---
    private void appendSo15(StringBuilder content, int nguHanhId, int thangSinh) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT KetQua, LoiGiai FROM so15_ruongdat WHERE NguHanhID = ? AND ThangSanh = ?", nguHanhId, thangSinh);
            if (!rows.isEmpty()) {
                String lg = (String) rows.get(0).get("LoiGiai");
                content.append("<p><strong>Đoán:</strong> ").append(rows.get(0).get("KetQua")).append("</p>");
                content.append("<p>").append(lg != null ? lg.replace("\n", "<br>") : "").append("</p>");
            } else { content.append("<p><em>(Chưa có dữ liệu)</em></p>"); }
        } catch (Exception e) {}
    }

    // --- SỞ 16: Thi cử ---
    private void appendSo16(StringBuilder content, int chiId, int thangSinh) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT TenTruc, LoiGiai FROM so16_hocgioido WHERE ChiID = ? AND ThangSanh = ?", chiId, thangSinh);
            if (!rows.isEmpty()) {
                String truc = (String) rows.get(0).get("TenTruc");
                String lg = (String) rows.get(0).get("LoiGiai");
                content.append("<p><strong>Học giỏi/dở:</strong> ").append(truc).append("</p>");
                content.append("<p>").append(lg != null ? lg.replace("\n", "<br>") : "").append("</p>");
            } else { content.append("<p><em>(Chưa có dữ liệu)</em></p>"); }
        } catch (Exception e) {}
    }

    // --- SỞ 17: Đi học ---
    private void appendSo17(StringBuilder content, int chiId, int thangSinh) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT TenTu, LoiGiai FROM so17_thicu_kynhat WHERE ChiID = ? AND ThangSanh = ?", chiId, thangSinh);
            if (!rows.isEmpty()) {
                String tu = (String) rows.get(0).get("TenTu");
                String lg = (String) rows.get(0).get("LoiGiai");
                content.append("<p><strong>Sao:</strong> ").append(tu).append("</p>");
                content.append("<p>").append(lg != null ? lg.replace("\n", "<br>") : "").append("</p>");
            } else { content.append("<p><em>(Chưa có dữ liệu)</em></p>"); }
        } catch (Exception e) {}
    }

    // --- SỞ 19: Phá sản ---
    private void appendSo19(StringBuilder content, int chiId, int thangSinh, String gioiTinh) {
        try {
            // Sửa lỗi so sánh giới tính: kiểm tra chuỗi chứa "nữ" cho "Nữ giới"
            String gt = gioiTinh != null && gioiTinh.toLowerCase().contains("nữ") ? "Nu" : "Nam";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT LoiGiai FROM so19_phasanvochong WHERE Tuoi_ChiID = ? AND GioiTinh = ? AND ThangSanh = ?", chiId, gt, thangSinh);
            if (!rows.isEmpty()) {
                String lg = (String) rows.get(0).get("LoiGiai");
                content.append("<p>").append(lg != null ? lg.replace("\n", "<br>") : "").append("</p>");
            } else { 
                content.append("<p><em>Số này không phạm phá sản của vợ/chồng.</em></p>"); 
            }
        } catch (Exception e) {}
    }

    // --- SỞ 20: Duyên nợ ---
    private void appendSo20(StringBuilder content, DivinationRequest request, int userCanId, int userChiId, int userThangSinh) {
        try {
            int chongNguHanhId = 0;
            int voThangSanh = 0;
            String partnerLunarInfo = "";

            boolean isMale = request.getGender() != null && request.getGender().toLowerCase().contains("nam");
            
            // Xử lý thông tin đối tác (Partner) - Quy đổi Dương sang Âm
            int pDay = 0, pMonth = 0, pYear = 0;
            try {
                pDay = Integer.parseInt(request.getPartnerBirthDay());
                pMonth = Integer.parseInt(request.getPartnerBirthMonth());
                pYear = Integer.parseInt(request.getPartnerBirthYear());
            } catch(Exception e) {}

            if (pDay > 0 && pMonth > 0 && pYear > 0) {
                LunarCalendarUtil.LunarDate pLunar = LunarCalendarUtil.convertSolarToLunar(pDay, pMonth, pYear, 7.0);
                int pLunarYear = pLunar.year;
                int pLunarMonth = pLunar.month;
                String pCanChi = LunarCalendarUtil.getCanChiString(pLunarYear);
                partnerLunarInfo = pCanChi + " (Tháng " + pLunarMonth + " Âm lịch)";

                if (isMale) {
                    // Người dùng là Chồng
                    chongNguHanhId = getLocalNguHanhId(userCanId, userChiId);
                    // Đối tác là Vợ
                    voThangSanh = pLunarMonth;
                } else {
                    // Người dùng là Vợ
                    voThangSanh = userThangSinh;
                    // Đối tác là Chồng
                    int pCanId = getDatabaseCanId(pLunarYear);
                    int pChiId = getDatabaseChiId(pLunarYear);
                    chongNguHanhId = getLocalNguHanhId(pCanId, pChiId);
                }
            }

            if (chongNguHanhId > 0 && voThangSanh > 0) {
                content.append("<p><strong>Đối tác:</strong> <span style='color: #b45309;'>").append(partnerLunarInfo).append("</span></p>");
                
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT KetQua FROM so20_ma_tran_duyen_no WHERE Chong_NguHanhID = ? AND Vo_ThangSanh = ?", 
                    chongNguHanhId, voThangSanh);
                
                if (!rows.isEmpty()) {
                    String kq = (String) rows.get(0).get("KetQua");
                    content.append("<p><strong>Kết quả:</strong> ").append(kq).append("</p>");
                    
                    List<Map<String, Object>> gRes = jdbcTemplate.queryForList(
                        "SELECT LoiGiai FROM so20_loi_giai_duyen_no WHERE KetQua = ?", kq);
                    if (!gRes.isEmpty()) {
                        String lg = (String) gRes.get(0).get("LoiGiai");
                        content.append("<p><strong>Luận giải:</strong> ").append(lg != null ? lg.replace("\n", "<br>") : "").append("</p>");
                    }
                } else {
                    content.append("<p><em>(Không tìm thấy tổ hợp duyên nợ này trong ma trận)</em></p>");
                }
            } else {
                content.append("<p style='color: #b45309;'><em>* Vui lòng bổ sung \"Thông tin Vợ/Chồng\" để xem chi tiết duyên nợ.</em></p>");
            }
        } catch(Exception e){
            content.append("<p><em>(Lỗi xử lý duyên nợ: ").append(e.getMessage()).append(")</em></p>");
        }
    }

    private int getLocalNguHanhId(int canId, int chiId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT NguHanhID FROM so04_tuoimang WHERE CanID = ? AND ChiID = ?", canId, chiId);
            if (!rows.isEmpty()) return (Integer) rows.get(0).get("NguHanhID");
        } catch(Exception e){}
        return 0;
    }



    // --- SỞ 21: Trường sanh ---
    private void appendSo21(StringBuilder content, int nguHanhId, int chiId) {
        try {
            int tsId = getTruongSanhID(nguHanhId, chiId);
            if (tsId > 0) {
                // Lấy tên sao từ bảng Sở 22 hoặc 23 (chung schema)
                List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT TenSao FROM so22_nuoicon WHERE TruongSanhID = ?", tsId);
                String sao = !rows.isEmpty() ? (String) rows.get(0).get("TenSao") : "Không rõ";
                content.append("<p><strong>Cung mạng:</strong> Đóng tại cung <span style='color: #b45309;'>").append(sao).append("</span></p>");
            } else { 
                content.append("<p><em>(Chưa phân tích được cung mạng Trường sanh)</em></p>"); 
            }
        } catch(Exception e){}
    }

    // --- SỞ 22: Sinh con ---
    private void appendSo22(StringBuilder content, int nguHanhId, int chiId) {
        try {
            int tsId = getTruongSanhID(nguHanhId, chiId);
            if (tsId > 0) {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT LoiGiai FROM so22_nuoicon WHERE TruongSanhID = ?", tsId);
                if (!rows.isEmpty()) {
                    content.append("<p><strong>Đoán rằng:</strong> ").append(rows.get(0).get("LoiGiai")).append("</p>");
                }
                
                // Thêm lời bàn luận chung
                List<Map<String, Object>> banLuan = jdbcTemplate.queryForList("SELECT NoiDung FROM so22_loi_ban_luan ORDER BY ID");
                if (!banLuan.isEmpty()) {
                    content.append("<div style='margin-top:10px; font-style: italic; font-size: 0.9rem; color: #6b7280;'>");
                    content.append("<strong>Lời bàn:</strong><br>");
                    for (Map<String, Object> bl : banLuan) {
                        content.append("- ").append(bl.get("NoiDung")).append("<br>");
                    }
                    content.append("</div>");
                }
            } else {
                content.append("<p><em>(Chưa có dữ liệu đoán con)</em></p>");
            }
        } catch(Exception e){}
    }

    // --- SỞ 23: Anh em ---
    private void appendSo23(StringBuilder content, int nguHanhId, int chiId) {
        try {
            int tsId = getTruongSanhID(nguHanhId, chiId);
            if (tsId > 0) {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT BaiTho FROM so23_anhem WHERE TruongSanhID = ?", tsId);
                if (!rows.isEmpty()) {
                    String tho = (String) rows.get(0).get("BaiTho");
                    content.append("<p style='white-space: pre-line; font-family: serif; font-style: italic; background: #fffcf2; padding: 10px; border-radius: 5px;'>").append(tho).append("</p>");
                }
            } else { 
                content.append("<p><em>(Chưa có dữ liệu bài thơ anh em)</em></p>"); 
            }
        } catch (Exception e) {}
    }

    private int getTruongSanhID(int nguHanhId, int chiId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT TruongSanhID FROM so21_truongsanh_mapping WHERE NguHanhID = ? AND ChiID = ?", nguHanhId, chiId);
            if (!rows.isEmpty()) return (Integer) rows.get(0).get("TruongSanhID");
        } catch(Exception e){}
        return 0;
    }

    // --- SỞ 18: Tù tội ---
    private void appendSo18(StringBuilder content, int canId, int chiId, int thangSinh) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT LoiGiai FROM so18_otuhaykhong WHERE Tuoi_CanID = ? AND Tuoi_ChiID = ? AND ThangSanh = ?", canId, chiId, thangSinh);
            if (!rows.isEmpty()) {
                String lg = (String) rows.get(0).get("LoiGiai");
                content.append("<p>").append(lg != null ? lg.replace("\n", "<br>") : "").append("</p>");
            } else { content.append("<p><em>(Chưa có dữ liệu)</em></p>"); }
        } catch(Exception e){}
    }

    // --- SỞ 28: Sao hạn ---
    private void appendSo28(StringBuilder content, String gioiTinh, int birthYear) {
         java.time.Year currentYear = java.time.Year.now();
         int tuoiMu = currentYear.getValue() - birthYear + 1;
         // Sửa logic so sánh giới tính tương tự
         String gt = gioiTinh != null && gioiTinh.toLowerCase().contains("nữ") ? "Nu" : "Nam";
         try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT TenSao FROM so28_saochieumenh WHERE TuoiAmLich = ? AND GioiTinh = ?", tuoiMu, gt);
            if (!rows.isEmpty()) {
                content.append("<p><strong>Tuổi mụ:</strong> ").append(tuoiMu).append(" - <strong>Sao chiếu:</strong> ").append(rows.get(0).get("TenSao")).append("</p>");
            } else { content.append("<p><em>(Năm nay chưa tính được sao)</em></p>"); }
         } catch(Exception e){}
    }

    // --- SỞ 29: Hạn năm ---
    private void appendSo29(StringBuilder content, String gioiTinh, int birthYear) {
         java.time.Year currentYear = java.time.Year.now();
         int tuoiMu = currentYear.getValue() - birthYear + 1;
         String gt = gioiTinh != null && gioiTinh.toLowerCase().contains("nữ") ? "Nu" : "Nam";
         try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT TenHan, LoiGiai FROM so29_hanhangnam WHERE TuoiAmLich = ? AND GioiTinh = ?", tuoiMu, gt);
            if (!rows.isEmpty()) {
                content.append("<p><strong>Hạn:</strong> ").append(rows.get(0).get("TenHan")).append("</p>");
                String lg = (String) rows.get(0).get("LoiGiai");
                content.append("<p>").append(lg != null ? lg.replace("\n", "<br>") : "").append("</p>");
            } else { content.append("<p><em>(Chưa có dữ liệu hạn)</em></p>"); }
         } catch(Exception e){}
    }

    // --- SỞ 24: Huynh Đệ (4 mùa) ---
    private void appendSo24(StringBuilder content, int chiId, int thangSinh) {
        try {
            String mua = "Xuan";
            if (thangSinh >= 4 && thangSinh <= 6) mua = "Ha";
            else if (thangSinh >= 7 && thangSinh <= 9) mua = "Thu";
            else if (thangSinh >= 10 || thangSinh <= 12) mua = "Dong";
            
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT ViTri FROM so24_huynhde_mapping WHERE Mua = ? AND ChiID = ?", mua, chiId);
            if (!rows.isEmpty()) {
                String vt = (String) rows.get(0).get("ViTri");
                content.append("<p><strong>Vị trí:</strong> sinh vào cung ").append(vt).append("</p>");
            } else { content.append("<p><em>(Chưa có dữ liệu 4 mùa)</em></p>"); }
        } catch(Exception e){}
    }

    // --- SỞ 25: Con vua ---
    private void appendSo25(StringBuilder content, int chiId, int thangSinh) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT TenVua, LoiGiai FROM so25_con_vua WHERE ChiID = ?", chiId);
            if (!rows.isEmpty()) {
                content.append("<p><strong>Sinh dưới trướng:</strong> ").append(rows.get(0).get("TenVua")).append("</p>");
                String lg = (String) rows.get(0).get("LoiGiai");
                content.append("<p>").append(lg != null ? lg.replace("\n", "<br>") : "").append("</p>");
            } else { content.append("<p><em>(Chưa có dữ liệu)</em></p>"); }
        } catch(Exception e){}
    }

    // --- SỞ 26: Có nhà hay không ---
    private void appendSo26(StringBuilder content, int chiId, int thangSinh) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT TenKetQua, LoiGiai FROM so26_conha WHERE ChiID = ? AND ThangSanh = ?", chiId, thangSinh);
            if (!rows.isEmpty()) {
                String kq = (String) rows.get(0).get("TenKetQua");
                if (kq != null) content.append("<p><strong>Tình trạng:</strong> ").append(kq).append("</p>");
                String lg = (String) rows.get(0).get("LoiGiai");
                if (lg != null) content.append("<p>").append(lg.replace("\n", "<br>")).append("</p>");
            } else { content.append("<p><em>(Chưa có dữ liệu)</em></p>"); }
        } catch(Exception e){}
    }

    // --- SỞ 27: Số mạng mỗi tuổi ---
    private void appendSo27(StringBuilder content, int canId, int chiId, String gioiTinh) {
        try {
            String gt = gioiTinh != null && gioiTinh.equalsIgnoreCase("Nữ") ? "Nu" : "Nam";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT Mang, Tho, Luan FROM so27_tongquan WHERE Tuoi_CanID = ? AND Tuoi_ChiID = ? AND GioiTinh = ?", canId, chiId, gt);
            if (!rows.isEmpty()) {
                String tho = (String) rows.get(0).get("Tho");
                String luan = (String) rows.get(0).get("Luan");
                if (tho != null) content.append("<p><strong>Thơ:</strong><br>").append(tho.replace("\n", "<br>")).append("</p>");
                if (luan != null) content.append("<p><strong>Luận:</strong><br>").append(luan.replace("\n", "<br>")).append("</p>");
            } else { content.append("<p><em>(Chưa có tổng quan)</em></p>"); }
        } catch(Exception e){}
    }

    // --- SỞ 30: Cầu tiên bà ---
    private void appendSo30(StringBuilder content, int chiId, int thangSinh) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT TenLoiKhuyen, NoiDung FROM so30_loikhuyen LIMIT 2");
            for (Map<String, Object> r : rows) {
                content.append("<p><strong>").append(r.get("TenLoiKhuyen")).append(":</strong> ").append(r.get("NoiDung")).append("</p>");
            }
        } catch(Exception e){}
    }

    // --- SỞ 31: Sống lâu/Quy Tiên ---
    private void appendSo31(StringBuilder content, int nguHanhId, int thangSinh) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT PhanTram FROM so31_mang_thang_phantram WHERE NguHanhID = ? AND ThangSanh = ?", nguHanhId, thangSinh);
            if (!rows.isEmpty()) {
                content.append("<p><strong>Tỷ lệ sinh:</strong> ").append(rows.get(0).get("PhanTram")).append("%</p>");
            } else { content.append("<p><em>(Chưa có dữ liệu)</em></p>"); }
        } catch(Exception e){}
    }

    // --- SỞ 32: Có hòm ---
    private void appendSo32(StringBuilder content, int chiId, int thangSinh) {
        content.append("<p><em>(Chưa có bảng so32)</em></p>");
    }

    // --- SỞ 34: Giác hồng trần ---
    private void appendSo34(StringBuilder content, int chiId, int thangSinh) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT TieuDe, NoiDung FROM so34_trietly LIMIT 1");
            if (!rows.isEmpty()) {
                String lg = (String) rows.get(0).get("NoiDung");
                content.append("<p><strong>Triết lý:</strong> ").append(lg != null ? lg.replace("\n", "<br>") : "").append("</p>");
            }
        } catch(Exception e){}
    }

    // ID Mapping Database Helper
    private int getDatabaseCanId(int year) {
        int[] canIdMap = {7, 8, 9, 10, 1, 2, 3, 4, 5, 6};
        int index = year % 10;
        if (index < 0) index += 10;
        return canIdMap[index];
    }

    private int getDatabaseChiId(int year) {
        int[] chiIdMap = {9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8};
        int index = year % 12;
        if (index < 0) index += 12;
        return chiIdMap[index];
    }

    private int getDatabaseChiIdForHour(String hour) {
        if (hour == null) return 7; // Mặc định Ngọ
        hour = hour.toLowerCase().trim();
        if (hour.startsWith("tý") || hour.startsWith("chuột") || hour.startsWith("ty1")) return 1;
        if (hour.startsWith("sửu") || hour.startsWith("trâu")) return 2;
        if (hour.startsWith("dần") || hour.startsWith("hổ") || hour.startsWith("cọp")) return 3;
        if (hour.startsWith("mão") || hour.startsWith("mẹo") || hour.startsWith("thỏ")) return 4;
        if (hour.startsWith("thìn") || hour.startsWith("rồng")) return 5;
        if (hour.startsWith("tỵ") || hour.startsWith("rắn") || hour.startsWith("ty2")) return 6;
        if (hour.startsWith("ngọ") || hour.startsWith("ngựa")) return 7;
        if (hour.startsWith("mùi") || hour.startsWith("dê")) return 8;
        if (hour.startsWith("thân") || hour.startsWith("khỉ")) return 9;
        if (hour.startsWith("dậu") || hour.startsWith("gà")) return 10;
        if (hour.startsWith("tuất") || hour.startsWith("chó")) return 11;
        if (hour.startsWith("hợi") || hour.startsWith("heo") || hour.startsWith("lợn")) return 12;
        return 7;
    }

    private String extractCanChi(String rawGio) {
        if (rawGio == null || rawGio.isEmpty()) return "Ngọ";
        int idx = rawGio.indexOf(" ");
        return idx > 0 ? rawGio.substring(0, idx).trim() : rawGio;
    }

    private String mapChiToCot(String chi) {
        if (chi == null) return "chuot";
        switch (chi.toLowerCase()) {
            case "tý": return "chuot";
            case "sửu": return "trau";
            case "dần": return "cop";
            case "mão": return "tho";
            case "thìn": return "rong";
            case "tỵ": return "ran";
            case "ngọ": return "ngua";
            case "mùi": return "de";
            case "thân": return "khi";
            case "dậu": return "ga";
            case "tuất": return "cho";
            case "hợi": return "heo";
            default: return "chuot";
        }
    }
}