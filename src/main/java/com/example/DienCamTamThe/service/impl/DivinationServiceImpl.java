package com.example.DienCamTamThe.service.impl;

import com.example.DienCamTamThe.entity.*;
import com.example.DienCamTamThe.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DivinationServiceImpl {

    @Autowired
    private BookSectionRepository sectionRepo;

    @Autowired
    private BookEntryRepository entryRepo;

    public String processDivination(DivinationRequest request) {
        StringBuilder content = new StringBuilder();
        content.append("<div style='text-align: left;'>");

        String category = request.getLookupCategory();
        if (category == null) category = "Tất cả";

        // Lấy hạng mục cần tra cứu, mặc định là Tất cả
        int birthYear = 1999;
        int ngaySinh = 1;
        int thangSinh = 1;
        try {
            birthYear = Integer.parseInt(request.getBirthYear());
            ngaySinh = Integer.parseInt(request.getBirthDay());
            thangSinh = Integer.parseInt(request.getBirthMonth());
        } catch (Exception e) {}

        String gioSinhFull = request.getBirthHour();
        String canChiGio = extractCanChi(gioSinhFull);
        int thangThoThai = thangSinh - 9;
        if (thangThoThai <= 0) thangThoThai += 12;

        String[] canhArray = {"canh", "tan", "nham", "quy", "giap", "at", "binh", "dinh", "mau", "ky"};
        String can = canhArray[birthYear % 10];
        String[] chiArray = {"than", "dau", "tuat", "hoi", "ty1", "suu", "dan", "mao", "thin", "ty2", "ngo", "mui"};
        String chi = chiArray[birthYear % 12];

        String cot = mapChiToCot(chi);
        String mang = calculateMenh(birthYear % 10, birthYear % 12);

        content.append("<div class='log-box'>");
        content.append("<strong>[Log Hệ Thống] Thông số nội suy:</strong><br>");
        content.append("- Bạn tuổi: ").append(can.toUpperCase()).append(" ").append(chi.toUpperCase()).append("<br>");
        content.append("- Mệnh (Ngũ Hành): ").append(mang.toUpperCase()).append("<br>");
        content.append("- Cốt (Xương con gì): ").append(cot.toUpperCase()).append("<br>");
        content.append("<em>=> Tiến hành rà soát chuyên mục: <b>").append(category).append("</b></em>");
        content.append("</div>");

        List<BookSection> sections = sectionRepo.findAllByOrderBySectionNoAsc();
        boolean foundAny = false;

        for (BookSection sec : sections) {
            if (!isSectionInCategory(sec, category)) continue;

            foundAny = true;
            content.append("<hr><h4>Sở ").append(sec.getSectionNo()).append(". ").append(sec.getTitle()).append("</h4>");

            if (sec.getSectionNo() == 8) {
                appendSo8(content, sec.getSectionCode(), canChiGio);
            } else if (sec.getSectionNo() == 9) {
                appendSo9(content, sec.getSectionCode(), ngaySinh);
            } else if (sec.getSectionNo() == 10) {
                appendSo10(content, sec.getSectionCode(), thangThoThai, thangSinh);
            } else {
                appendOtherSections(content, sec, cot, mang);
            }
        }

        if (!foundAny) {
             content.append("<p>Không có dữ liệu chi tiết cho hạng mục này.</p>");
        }

        content.append("</div>");
        return content.toString();
    }

    private boolean isSectionInCategory(BookSection sec, String filterCategory) {
        int secNo = sec.getSectionNo();
        String dbCat = sec.getCategory() != null ? sec.getCategory() : "";

        if (filterCategory.equals("Tất cả")) {
            return true;
        }
        if (filterCategory.contains("Tình cảm")) {
            return dbCat.equals("hon_nhan") || dbCat.equals("gia_dao") || secNo == 19 || secNo == 20 || secNo == 22 || secNo == 23;
        }
        if (filterCategory.contains("Công danh")) {
            return dbCat.equals("nghe_nghiep") || dbCat.equals("thi_cu") || dbCat.equals("hoc_hanh") || secNo == 11 || secNo == 13 || secNo == 16 || secNo == 17;
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
            return dbCat.equals("van_menh") || secNo == 8 || secNo == 9 || secNo == 10 || secNo == 12 || secNo == 25 || secNo == 34;
        }
        return true;
    }

    // --- SỞ 8: Giờ sinh tam thế ---
    // Tìm kiếm lời đoán dựa trên giờ sinh của người dùng
    private void appendSo8(StringBuilder content, String sectionCode, String canChiGio) {
        List<BookEntry> entries = entryRepo.findBySectionCode(sectionCode);
        if (entries.isEmpty()) {
            content.append("<p><em>(Dữ liệu Sở 8 đang được cập nhật...)</em></p>");
            return;
        }

        BookEntry matched = null;
        // Tìm entry có input_data chứa giờ sinh khớp
        for (BookEntry e : entries) {
            String inputStr = e.getInputData() != null ? e.getInputData().toLowerCase() : "";
            if (canChiGio != null && inputStr.contains(canChiGio.toLowerCase())) {
                matched = e;
                break;
            }
        }
        // Fallback: entry đầu tiên
        if (matched == null) matched = entries.get(0);

        if (matched.getOutputData() != null) {
            // Hiển thị giờ sinh từ input_data
            String tenGio = extractJsonStringField(matched.getInputData(), "gio_sinh");
            if (tenGio == null || tenGio.isEmpty()) tenGio = canChiGio;
            content.append("<p><strong>Giờ sinh:</strong> ").append(tenGio).append("</p>");
            content.append(parseOutputJsonToText(matched.getOutputData()));
        } else {
            content.append("<p><em>(Không tìm thấy lời đoán cho giờ ").append(canChiGio).append(")</em></p>");
        }
    }

    // --- SỞ 9: Hiệu ngày ---
    // Tra cứu tên hiệu và lời đoán dựa trên ngày sinh âm lịch
    private void appendSo9(StringBuilder content, String sectionCode, int ngaySinh) {
        List<BookEntry> entries = entryRepo.findBySectionCode(sectionCode);
        if (entries.isEmpty()) {
            content.append("<p><em>(Dữ liệu Sở 9 đang được cập nhật...)</em></p>");
            return;
        }

        BookEntry matched = null;
        String foundTenHieu = null;

        // Tìm entry có input_data chứa ngày sinh
        for (BookEntry e : entries) {
            String inputStr = e.getInputData() != null ? e.getInputData() : "";
            // Kiểm tra dạng JSON: "ngay": 15 hoặc "ngay_am_lich": "...,15,..."
            String ngayField = extractJsonStringField(inputStr, "ngay_am_lich");
            String ngayNum = extractJsonNumberField(inputStr, "ngay");

            boolean matched1 = ngayField != null && containsDayNumber(ngayField, ngaySinh);
            boolean matched2 = ngayNum != null && Integer.parseInt(ngayNum) == ngaySinh;

            if (matched1 || matched2) {
                matched = e;
                foundTenHieu = extractJsonStringField(inputStr, "ten_hieu");
                break;
            }
        }

        if (matched == null) matched = entries.get(0);

        if (foundTenHieu != null && !foundTenHieu.isEmpty()) {
            content.append("<p><strong>Hiệu ngày:</strong> ").append(foundTenHieu).append("</p>");
        }
        if (matched.getOutputData() != null) {
            content.append(parseOutputJsonToText(matched.getOutputData()));
        } else {
            content.append("<p><em>(Không tìm thấy lời đoán cho ngày ").append(ngaySinh).append(")</em></p>");
        }
    }

    // --- SỞ 10: Tổng luận ---
    // Đưa ra lời đoán tổng quát dựa trên tháng thọ thai và tháng sinh
    private void appendSo10(StringBuilder content, String sectionCode, int thangThoThai, int thangSinh) {
        List<BookEntry> entries = entryRepo.findBySectionCode(sectionCode);
        if (entries.isEmpty()) {
            content.append("<p><em>(Dữ liệu Sở 10 đang được cập nhật...)</em></p>");
            return;
        }

        BookEntry matched = null;
        // Tìm entry khớp tháng thọ thai + tháng sinh
        for (BookEntry e : entries) {
            String inputStr = e.getInputData() != null ? e.getInputData() : "";
            String ttField  = extractJsonNumberField(inputStr, "thang_tho_thai");
            String tsField  = extractJsonNumberField(inputStr, "thang_sinh");

            boolean matchTT = ttField != null && Integer.parseInt(ttField) == thangThoThai;
            boolean matchTS = tsField != null && Integer.parseInt(tsField) == thangSinh;

            if (matchTT && matchTS) { matched = e; break; }
            if (matchTT && tsField == null) { matched = e; break; } // chỉ có trường tháng thọ thai
        }
        // Fallback tìm theo tháng thọ thai
        if (matched == null) {
            for (BookEntry e : entries) {
                String inputStr = e.getInputData() != null ? e.getInputData() : "";
                String ttField = extractJsonNumberField(inputStr, "thang_tho_thai");
                if (ttField != null && Integer.parseInt(ttField) == thangThoThai) {
                    matched = e; break;
                }
            }
        }
        if (matched == null) matched = entries.get(0);

        content.append("<p><em>Tháng thọ thai: ").append(thangThoThai)
               .append(" | Tháng sinh: ").append(thangSinh).append("</em></p>");
        if (matched.getOutputData() != null) {
            content.append(parseOutputJsonToText(matched.getOutputData()));
        } else {
            content.append("<p><em>(Không tìm thấy lời đoán tương ứng)</em></p>");
        }
    }

    // --- Xử lý các sở khác ---
    // Áp dụng logic tra cứu chung cho các mục còn lại (theo Cốt, Mệnh, Tuổi...)
    private void appendOtherSections(StringBuilder content, BookSection sec, String cot, String mang) {
        List<BookEntry> entries = entryRepo.findBySectionCode(sec.getSectionCode());
        if (entries.isEmpty()) {
            content.append("<p><em>(Dữ liệu đang được cập nhật...)</em></p>");
            return;
        }

        BookEntry matched = null;
        String vietName = mapChiToVietnamese(mapCotToChi(cot)); // Lấy tên tiếng Việt (Tý, Sửu...)

        for (BookEntry e : entries) {
            String inJson = e.getInputData();
            if (inJson == null || inJson.isBlank()) continue;

            // 1. Kiểm tra "tat_ca"
            if (inJson.contains("tat_ca")) { matched = e; break; }

            // 2. Kiểm tra "cot"
            String eCot = extractJsonStringField(inJson, "cot");
            if (eCot != null && eCot.equalsIgnoreCase(cot)) { matched = e; break; }

            // 3. So khớp theo Mạng (Hoả, Thổ, Kim...)
            String eMang = extractJsonStringField(inJson, "mang");
            if (eMang == null) eMang = extractJsonStringField(inJson, "menh");
            if (eMang != null && eMang.equalsIgnoreCase(mang)) { matched = e; break; }

            // 4. Kiểm tra "tuoi" (So khớp tiếng Việt có dấu: Tý, Sửu...)
            String eTuoi = extractJsonStringField(inJson, "tuoi");
            if (eTuoi != null && eTuoi.equalsIgnoreCase(vietName)) { matched = e; break; }
            
            // 5. Kiểm tra "sao" hoặc "nghe"
            if (inJson.contains("\"sao\"") || inJson.contains("\"nghe\"")) {
                // Nếu là bảng tra chung thì cho qua, nếu là text cụ thể thì cần thêm logic filter
                // Hiện tại lấy entry đầu tiên khớp format
                matched = e; 
            }
        }

        if (matched == null) matched = entries.get(0);

        if (matched != null && matched.getOutputData() != null) {
            content.append(parseOutputJsonToText(matched.getOutputData()));
        } else {
            content.append("<p><em>(Dữ liệu không khớp hoặc đang phân tích...)</em></p>");
        }
    }

    private String mapCotToChi(String cot) {
        switch (cot) {
            case "chuot": return "ty1";
            case "trau": return "suu";
            case "cop": return "dan";
            case "tho": return "mao";
            case "rong": return "thin";
            case "ran": return "ty2";
            case "ngua": return "ngo";
            case "de": return "mui";
            case "khi": return "than";
            case "ga": return "dau";
            case "cho": return "tuat";
            case "heo": return "hoi";
            default: return cot;
        }
    }

    // --- Các hàm hỗ trợ xử lý dữ liệu ---

    private String parseOutputJsonToText(String rawOut) {
        if (rawOut == null || rawOut.isBlank()) return "";
        if (!rawOut.trim().startsWith("{")) {
            return "<p>" + rawOut.replace("\n", "<br>") + "</p>";
        }
        try {
            StringBuilder sb = new StringBuilder();

            String loiDoan = extractJsonStringField(rawOut, "loi_doan");
            if (loiDoan != null && !loiDoan.isEmpty()) {
                sb.append("<p><strong>Lời đoán:</strong> ").append(loiDoan).append("</p>");
            }

            String summary = extractJsonStringField(rawOut, "result_summary");
            if (summary != null && !summary.isEmpty()) {
                sb.append("<p><strong>Tổng quan:</strong> ").append(summary).append("</p>");
            }

            String text = extractJsonStringField(rawOut, "result_text");
            if (text != null && !text.isEmpty()) {
                sb.append("<p>").append(text.replace("\n", "<br>")).append("</p>");
            }

            String poem = extractJsonStringField(rawOut, "poem");
            if (poem != null && !poem.isEmpty()) {
                sb.append("<blockquote class='poem-box'>")
                  .append(poem.replace("\n", "<br>"))
                  .append("</blockquote>");
            }

            if (sb.length() == 0) {
                return "<p>" + rawOut.replace("\n", "<br>") + "</p>";
            }
            return sb.toString();
        } catch (Exception e) {
            return "<p>" + rawOut.replace("\n", "<br>") + "</p>";
        }
    }

    /** Hàm trích xuất giá trị chuỗi từ định dạng JSON đơn giản */
    private String extractJsonStringField(String json, String field) {
        if (json == null) return null;
        String search = "\"" + field + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) { search = "\"" + field + "\" :"; idx = json.indexOf(search); }
        if (idx < 0) return null;

        int startQuote = json.indexOf("\"", idx + search.length());
        if (startQuote < 0) return null;
        int endQuote = json.indexOf("\"", startQuote + 1);
        while (endQuote > 0 && json.charAt(endQuote - 1) == '\\') {
            endQuote = json.indexOf("\"", endQuote + 1);
        }
        if (endQuote < 0) return null;

        String val = json.substring(startQuote + 1, endQuote);
        val = val.replace("\\n", "<br>").replace("\\\"", "\"").replace("\\\\", "\\");
        return val;
    }

    /** Hàm trích xuất giá trị số từ định dạng JSON đơn giản */
    private String extractJsonNumberField(String json, String field) {
        if (json == null) return null;
        String search = "\"" + field + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) { search = "\"" + field + "\" :"; idx = json.indexOf(search); }
        if (idx < 0) return null;

        int start = idx + search.length();
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (start >= json.length()) return null;

        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        if (end == start) return null;
        return json.substring(start, end);
    }

    /** Kiểm tra chuỗi ngày "1, 2, 15, 30" có chứa ngày ngaySinh không */
    private boolean containsDayNumber(String ngayStr, int ngaySinh) {
        String target = String.valueOf(ngaySinh);
        String[] parts = ngayStr.split("[,\\s]+");
        for (String p : parts) {
            if (p.trim().equals(target)) return true;
        }
        // fallback: kiểm tra dạng plain " 15 " hay "15,"
        return ngayStr.contains(" " + ngaySinh + ",")
            || ngayStr.contains(" " + ngaySinh)
            || ngayStr.equals(target);
    }

    // --- Thuật toán tính toán thông số Diễn Cầm ---
    private String extractCanChi(String rawGio) {
        if (rawGio == null || rawGio.isEmpty()) return "Ngọ";
        int idx = rawGio.indexOf(" ");
        return idx > 0 ? rawGio.substring(0, idx).trim() : rawGio;
    }

    private String mapChiToCot(String chi) {
        switch (chi) {
            case "ty1": return "chuot";
            case "suu": return "trau";
            case "dan": return "cop";
            case "mao": return "tho";
            case "thin": return "rong";
            case "ty2": return "ran";
            case "ngo": return "ngua";
            case "mui": return "de";
            case "than": return "khi";
            case "dau": return "ga";
            case "tuat": return "cho";
            case "hoi": return "heo";
            default: return "chuot";
        }
    }

    private String calculateMenh(int canIndex, int chiIndex) {
        int canVal = 0;
        if (canIndex == 4 || canIndex == 5) canVal = 1;
        else if (canIndex == 6 || canIndex == 7) canVal = 2;
        else if (canIndex == 8 || canIndex == 9) canVal = 3;
        else if (canIndex == 0 || canIndex == 1) canVal = 4;
        else if (canIndex == 2 || canIndex == 3) canVal = 5;

        int chiVal = 0;
        if (chiIndex == 4 || chiIndex == 5 || chiIndex == 10 || chiIndex == 11) chiVal = 0;
        else if (chiIndex == 6 || chiIndex == 7 || chiIndex == 0 || chiIndex == 1) chiVal = 1;
        else chiVal = 2;

        int finalMenh = canVal + chiVal;
        if (finalMenh > 5) finalMenh -= 5;

        switch (finalMenh) {
            case 1: return "kim";
            case 2: return "thuy";
            case 3: return "hoa";
            case 4: return "tho";
            case 5: return "moc";
        }
        return "kim";
    }

    private String mapChiToVietnamese(String chi) {
        if (chi == null) return "";
        switch (chi.toLowerCase()) {
            case "ty1": return "Tý";
            case "suu": return "Sửu";
            case "dan": return "Dần";
            case "mao": return "Mẹo";
            case "thin": return "Thìn";
            case "ty2": return "Tỵ";
            case "ngo": return "Ngọ";
            case "mui": return "Mùi";
            case "than": return "Thân";
            case "dau": return "Dậu";
            case "tuat": return "Tuất";
            case "hoi": return "Hợi";
            default: return chi;
        }
    }
}
