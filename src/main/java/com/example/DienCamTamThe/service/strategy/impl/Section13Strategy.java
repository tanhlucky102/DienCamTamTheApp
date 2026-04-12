package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section13Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 13;
    }

    @Override
    public String getSectionTitle() {
        return "Coi tuổi vợ con nít sanh";
    }

    @Override
    public void process(StringBuilder content, DivinationRequest request, String can, String chi, int ngaySinh, int thangSinh, String canChiGio, int thangThoThai, String mang, String cot, int truongSanhId, String gioSinhFull) {
        int canId = getCanId(can);
        int chiId = getChiId(chi);
        int mangId = getNguHanhId(mang);
        String mua = getMua(thangSinh);
        String buoi = getBuoi(gioSinhFull);
        boolean foundContent = false;
        

            try {
                // Tiêu đề phần Can
                content.append("<p><strong>Can ").append(can).append(" Sinh Tháng ").append(thangSinh)
                        .append("</strong></p>");

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
                content.append("<p><strong>Chi ").append(chi).append(" Sinh Tháng ").append(thangSinh)
                        .append("</strong></p>");

                // 5. so13_chi_sat
                appendSo13TableResult(content, "so13_chi_sat", "ChiID", chiId, thangSinh);
                // 6. so13_chi_thu
                appendSo13TableResult(content, "so13_chi_thu", "ChiID", chiId, thangSinh);

                foundContent = true;
                /* return block */
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 13: " + e.getMessage());
            }
        
    }
}
