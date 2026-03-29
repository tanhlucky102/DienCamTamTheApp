package com.example.diencamtamthe.service;

import com.example.diencamtamthe.model.*;
import com.example.diencamtamthe.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DivinationService {

    @Autowired
    private BookSectionRepository sectionRepo;

    @Autowired
    private BookEntryRepository entryRepo;

    @Autowired
    private So8GioSinhTamTheRepository so8Repo;

    @Autowired
    private So9HieuNgayRepository so9HieuNgayRepo;

    @Autowired
    private So9LoiDoanRepository so9LoiDoanRepo;

    @Autowired
    private So10TongLuanRepository so10Repo;

    public String processDivination(DivinationRequest request) {
        StringBuilder content = new StringBuilder();
        content.append("<div style='text-align: left;'>");

        String category = request.getLookupCategory();
        if (category == null) category = "Tất cả";

        // Xử lý dữ liệu đầu vào
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

        content.append("<div style='background: #f4f4f4; padding: 10px; border-radius: 5px; margin-bottom: 15px; color: #333'>");
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
                appendSo8(content, canChiGio);
            } else if (sec.getSectionNo() == 9) {
                appendSo9(content, ngaySinh);
            } else if (sec.getSectionNo() == 10) {
                appendSo10(content, thangThoThai, thangSinh);
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
            return dbCat.equals("hon_nhan") || dbCat.equals("gia_dao");
        }
        if (filterCategory.contains("Công danh")) {
            return dbCat.equals("nghe_nghiep") || dbCat.equals("thi_cu") || dbCat.equals("hoc_hanh");
        }
        if (filterCategory.contains("Tài lộc")) {
            return dbCat.equals("nha_dat") || dbCat.equals("khac");
        }
        if (filterCategory.contains("Sức khỏe")) {
            return dbCat.equals("cung_kien");
        }
        if (filterCategory.contains("Gia đình")) {
            return dbCat.equals("anh_em") || dbCat.equals("con_cai");
        }
        if (filterCategory.contains("Vận hạn")) {
            return dbCat.equals("van_menh");
        }
        if (filterCategory.contains("Bản thân")) {
            return dbCat.equals("van_menh") || secNo == 8 || secNo == 9 || secNo == 10;
        }
        return true;
    }

    private void appendOtherSections(StringBuilder content, BookSection sec, String cot, String mang) {
        List<BookEntry> entries = entryRepo.findBySectionCode(sec.getSectionCode());
        if (entries.isEmpty()) {
            content.append("<p><em>(Dữ liệu đang được cập nhật...)</em></p>");
            return;
        }

        BookEntry exactCot = null;
        BookEntry exactMang = null;
        BookEntry tatCa = null;

        for (BookEntry e : entries) {
            String inJson = e.getInputData() != null ? e.getInputData().toLowerCase().trim() : "";
            if (inJson.contains("\"cot\":") && inJson.contains("\"" + cot + "\"")) exactCot = e;
            if (inJson.contains("\"mang\":") && inJson.contains("\"" + mang + "\"")) exactMang = e;
            if (inJson.contains("tat_ca")) tatCa = e;
        }

        BookEntry target = exactCot != null ? exactCot : (exactMang != null ? exactMang : (tatCa != null ? tatCa : entries.get(0)));
        
        if (target != null && target.getOutputData() != null) {
            String rawOut = target.getOutputData();
            String parsedText = parseOutputJsonToText(rawOut);
            content.append(parsedText);
        } else {
            content.append("<p><em>(Dữ liệu không khớp hoặc đang phân tích...)</em></p>");
        }
    }

    private String parseOutputJsonToText(String rawOut) {
        if (!rawOut.trim().startsWith("{")) {
            return "<p>" + rawOut.replace("\n", "<br>") + "</p>";
        }
        try {
            StringBuilder sb = new StringBuilder();
            
            String summary = extractJsonStringField(rawOut, "result_summary");
            if (summary != null && !summary.isEmpty()) {
                sb.append("<p><strong>Tổng quan:</strong> ").append(summary).append("</p>");
            }
            
            String text = extractJsonStringField(rawOut, "result_text");
            if (text != null && !text.isEmpty()) {
                sb.append("<p>").append(text).append("</p>");
            }
            
            String poem = extractJsonStringField(rawOut, "poem");
            if (poem != null && !poem.isEmpty()) {
                sb.append("<blockquote style='font-style: italic; background: #fafafa; color: #333; border-left: 4px solid #ccc; padding: 10px; margin: 10px 0;'>")
                  .append(poem)
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

    private String extractJsonStringField(String json, String field) {
        String search = "\"" + field + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) search = "\"" + field + "\" :";
        idx = json.indexOf(search);
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

    // --- Thuật toán Diễn Cầm ---
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
    
    // Tính toán Mạng Ngũ Hành dựa trên Lục Thập Hoa Giáp
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

    private void appendSo8(StringBuilder content, String canChiGio) {
        List<So8GioSinhTamThe> so8List = so8Repo.findAll();
        So8GioSinhTamThe matchedSo8 = null;
        for (So8GioSinhTamThe so8 : so8List) {
            String dbGio = so8.getGioSinh() != null ? so8.getGioSinh().toLowerCase() : "";
            if (dbGio.contains(canChiGio.toLowerCase()) && 
               (so8.getPhanGio() == null || so8.getPhanGio().toLowerCase().contains("giữa"))) {
                matchedSo8 = so8; break;
            }
        }
        if (matchedSo8 != null && matchedSo8.getLoiDoan() != null) {
            content.append("<p><strong>Giờ sinh:</strong> ").append(matchedSo8.getGioSinh()).append("</p>");
            content.append("<p><strong>Lời đoán:</strong> ").append(matchedSo8.getLoiDoan().replace("\n", "<br>")).append("</p>");
        }
    }

    private void appendSo9(StringBuilder content, int ngaySinh) {
        List<So9HieuNgay> hieuNgayList = so9HieuNgayRepo.findAll();
        String foundTenHieu = null;
        for (So9HieuNgay hn : hieuNgayList) {
            String strNgay = hn.getNgayAmLich();
            if (strNgay != null && (strNgay.contains(" " + ngaySinh + ",") || strNgay.contains(" " + ngaySinh) || strNgay.equals(String.valueOf(ngaySinh)))) {
                foundTenHieu = hn.getTenHieu(); break;
            }
        }
        if (foundTenHieu != null) {
            List<So9LoiDoan> so9DoanList = so9LoiDoanRepo.findByTenHieu(foundTenHieu);
            if (!so9DoanList.isEmpty()) {
                content.append("<p><strong>Lời đoán:</strong> ").append(so9DoanList.get(0).getLoiDoan().replace("\n", "<br>")).append("</p>");
            }
        }
    }

    private void appendSo10(StringBuilder content, int thangThoThai, int thangSinh) {
        List<So10TongLuan> so10List = so10Repo.findByThangThoThaiAndThangSinh(thangThoThai, thangSinh);
        if (!so10List.isEmpty()) {
            content.append("<p><strong>Lời đoán:</strong> ").append(so10List.get(0).getLoiDoan().replace("\n", "<br>")).append("</p>");
        }
    }
}
