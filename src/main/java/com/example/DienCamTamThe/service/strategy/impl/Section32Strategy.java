package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section32Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 32;
    }

    @Override
    public String getSectionTitle() {
        return "Coi khi chết";
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
            
        
    }
}
