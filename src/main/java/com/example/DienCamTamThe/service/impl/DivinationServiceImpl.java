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
        // Fallback: entry đầu tiên
        if (matched == null)
            matched = entries.get(0);

        if (matched.getOutputData() != null) {
            String tenGio = extractJsonStringField(matched.getInputData(), "gio_sinh");
            if (tenGio == null || tenGio.isEmpty())
                tenGio = canChiGio;
            content.append("<p><strong>Giờ sinh:</strong> ").append(tenGio).append("</p>");
            content.append(parseOutputJsonToText(matched.getOutputData()));
        } else {
            content.append("<p><em>(Không tìm thấy lời đoán cho giờ ").append(canChiGio).append(")</em></p>");
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

        if (matched == null)
            matched = entries.get(0);

        if (foundTenHieu != null && !foundTenHieu.isEmpty()) {
            content.append("<p><strong>Hiệu ngày:</strong> ").append(foundTenHieu).append("</p>");
        }
        if (matched.getOutputData() != null) {
            content.append(parseOutputJsonToText(matched.getOutputData()));
        } else {
            content.append("<p><em>(Không tìm thấy lời đoán cho ngày ").append(ngaySinh).append(")</em></p>");
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

            if (matchTT && matchTS) {
                matched = e;
                break;
            }
            if (matchTT && tsField == null) {
                matched = e;
                break;
            } // chỉ có trường tháng thọ thai
        }
        if (matched == null) {
            for (BookEntry e : entries) {
                String inputStr = e.getInputData() != null ? e.getInputData() : "";
                String ttField = extractJsonNumberField(inputStr, "thang_tho_thai");
                if (ttField != null && Integer.parseInt(ttField) == thangThoThai) {
                    matched = e;
                    break;
                }
            }
        }
        if (matched == null)
            matched = entries.get(0);

        content.append("<p><em>Tháng thọ thai: ").append(thangThoThai)
                .append(" | Tháng sinh: ").append(thangSinh).append("</em></p>");
        if (matched.getOutputData() != null) {
            content.append(parseOutputJsonToText(matched.getOutputData()));
        } else {
            content.append("<p><em>(Không tìm thấy lời đoán tương ứng)</em></p>");
        }
    }

    private int getLocalNguHanhId(int canId, int chiId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT NguHanhID FROM so04_tuoimang WHERE CanID = ? AND ChiID = ?", canId, chiId);
            if (!rows.isEmpty()) return (Integer) rows.get(0).get("NguHanhID");
        } catch(Exception e){}
        return 0;
    }


        for (BookEntry e : entries) {
            String inJson = e.getInputData();
            if (inJson == null || inJson.isBlank())
                continue;

            // 1. Kiểm tra "tat_ca"
            if (inJson.contains("tat_ca")) {
                matched = e;
                break;
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

            String eMang = extractJsonStringField(inJson, "mang");
            if (eMang == null)
                eMang = extractJsonStringField(inJson, "menh");
            if (eMang != null && eMang.equalsIgnoreCase(mang)) {
                matched = e;
                break;
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

            // 5. Kiểm tra "sao" hoặc "nghe"
            if (inJson.contains("\"sao\"") || inJson.contains("\"nghe\"")) {
                // Nếu là bảng tra chung thì cho qua, nếu là text cụ thể thì cần thêm logic
                // filter
                // Hiện tại lấy entry đầu tiên khớp format
                matched = e;
            }
        }

        if (matched == null)
            matched = entries.get(0);

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

    private String mapCotToChi(String cot) {
        switch (cot) {
            case "chuot":
                return "ty1";
            case "trau":
                return "suu";
            case "cop":
                return "dan";
            case "tho":
                return "mao";
            case "rong":
                return "thin";
            case "ran":
                return "ty2";
            case "ngua":
                return "ngo";
            case "de":
                return "mui";
            case "khi":
                return "than";
            case "ga":
                return "dau";
            case "cho":
                return "tuat";
            case "heo":
                return "hoi";
            default:
                return cot;
        }
    }

    private String parseOutputJsonToText(String rawOut) {
        if (rawOut == null || rawOut.isBlank())
            return "";
        if (!rawOut.trim().startsWith("{")) {
            return "<p>" + rawOut.replace("\n", "<br>") + "</p>";
        }
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
            String poem = extractJsonStringField(rawOut, "poem");
            if (poem != null && !poem.isEmpty()) {
                sb.append("<blockquote class='poem-box'>")
                        .append(poem.replace("\n", "<br>"))
                        .append("</blockquote>");
            }
        } catch(Exception e){}
    }

    private String extractJsonStringField(String json, String field) {
        if (json == null)
            return null;
        String search = "\"" + field + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) {
            search = "\"" + field + "\" :";
            idx = json.indexOf(search);
        }
        if (idx < 0)
            return null;

        int startQuote = json.indexOf("\"", idx + search.length());
        if (startQuote < 0)
            return null;
        int endQuote = json.indexOf("\"", startQuote + 1);
        while (endQuote > 0 && json.charAt(endQuote - 1) == '\\') {
            endQuote = json.indexOf("\"", endQuote + 1);
        }
        if (endQuote < 0)
            return null;

        String val = json.substring(startQuote + 1, endQuote);
        val = val.replace("\\n", "<br>").replace("\\\"", "\"").replace("\\\\", "\\");
        return val;
    }

    private String extractJsonNumberField(String json, String field) {
        if (json == null)
            return null;
        String search = "\"" + field + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) {
            search = "\"" + field + "\" :";
            idx = json.indexOf(search);
        }
        if (idx < 0)
            return null;

        int start = idx + search.length();
        while (start < json.length() && json.charAt(start) == ' ')
            start++;
        if (start >= json.length())
            return null;

        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-'))
            end++;
        if (end == start)
            return null;
        return json.substring(start, end);
    }

    private boolean containsDayNumber(String ngayStr, int ngaySinh) {
        String target = String.valueOf(ngaySinh);
        String[] parts = ngayStr.split("[,\\s]+");
        for (String p : parts) {
            if (p.trim().equals(target))
                return true;
        }
        // fallback: kiểm tra dạng plain " 15 " hay "15,"
        return ngayStr.contains(" " + ngaySinh + ",")
                || ngayStr.contains(" " + ngaySinh)
                || ngayStr.equals(target);
    }

    private String extractCanChi(String rawGio) {
        if (rawGio == null || rawGio.isEmpty())
            return "Ngọ";
        int idx = rawGio.indexOf(" ");
        return idx > 0 ? rawGio.substring(0, idx).trim() : rawGio;
    }

    private String mapChiToCot(String chi) {
        switch (chi) {
            case "ty1":
                return "chuot";
            case "suu":
                return "trau";
            case "dan":
                return "cop";
            case "mao":
                return "tho";
            case "thin":
                return "rong";
            case "ty2":
                return "ran";
            case "ngo":
                return "ngua";
            case "mui":
                return "de";
            case "than":
                return "khi";
            case "dau":
                return "ga";
            case "tuat":
                return "cho";
            case "hoi":
                return "heo";
            default:
                return "chuot";
        }
    }

    private int getCanIndex(String can) {
        String[] cans = {"canh", "tân", "nhâm", "quý", "giáp", "ất", "bính", "đinh", "mậu", "kỷ"};
        for (int i = 0; i < cans.length; i++) {
            if (cans[i].equalsIgnoreCase(can)) return i;
        }
        return 0;
    }

    private int getChiIndex(String chi) {
        String[] chis = {"thân", "dậu", "tuất", "hợi", "tý", "sửu", "dần", "mão", "thìn", "tỵ", "ngọ", "mùi"};
        for (int i = 0; i < chis.length; i++) {
            if (chis[i].equalsIgnoreCase(chi)) return i;
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

    private String mapChiToVietnamese(String chi) {
        if (chi == null)
            return "";
        switch (chi.toLowerCase()) {
            case "ty1":
                return "Tý";
            case "suu":
                return "Sửu";
            case "dan":
                return "Dần";
            case "mao":
                return "Mẹo";
            case "thin":
                return "Thìn";
            case "ty2":
                return "Tỵ";
            case "ngo":
                return "Ngọ";
            case "mui":
                return "Mùi";
            case "than":
                return "Thân";
            case "dau":
                return "Dậu";
            case "tuat":
                return "Tuất";
            case "hoi":
                return "Hợi";
            default:
                return chi;
        }
    }
}