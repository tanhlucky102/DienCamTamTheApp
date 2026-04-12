package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section30Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 30;
    }

    @Override
    public String getSectionTitle() {
        return "Coi lời khuyên & nghi lễ";
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
            
        
    }
}
