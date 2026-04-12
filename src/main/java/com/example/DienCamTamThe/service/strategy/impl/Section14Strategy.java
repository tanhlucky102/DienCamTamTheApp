package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section14Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 14;
    }

    @Override
    public String getSectionTitle() {
        return "Coi nuôi thú vật";
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
                    /* return block */
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 14: " + e.getMessage());
            }
        
    }
}
