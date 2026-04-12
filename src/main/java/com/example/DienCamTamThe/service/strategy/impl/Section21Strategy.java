package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section21Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 21;
    }

    @Override
    public String getSectionTitle() {
        return "Coi Vòng Trường Sinh";
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
            /* return block */
        
    }
}
