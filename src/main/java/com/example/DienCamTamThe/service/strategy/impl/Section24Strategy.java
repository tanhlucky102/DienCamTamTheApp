package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section24Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 24;
    }

    @Override
    public String getSectionTitle() {
        return "Coi huynh đệ (Tài lộc)";
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
                String sql1 = "SELECT ViTri FROM so24_huynhde_mapping WHERE mua = ? AND ChiID = ?";
                List<?> vitriList = entityManager.createNativeQuery(sql1)
                        .setParameter(1, mua).setParameter(2, chiId).getResultList();

                if (!vitriList.isEmpty()) {
                    String vitri = vitriList.get(0).toString();
                    content.append("<p style='text-align: center; margin-bottom: 15px;'>")
                            .append("<span style='color: #1a5fb4; font-weight: bold; font-size: 1.1em;'>Mùa ")
                            .append(mua).append(" tuổi ").append(chi).append("</span><br>")
                            .append("<span style='color: #b8860b; font-weight: bold; font-size: 1.1em;'>Ở tại ")
                            .append(vitri).append("</span>")
                            .append("</p>");

                    String sql2 = "SELECT BaiTho FROM so24_huynhde_loigiai WHERE ViTri = ?";
                    List<?> thoList = entityManager.createNativeQuery(sql2)
                            .setParameter(1, vitri).getResultList();

                    for (Object obj : thoList) {
                        if (obj != null) {
                            String tho = obj.toString();
                            String[] lines = tho.split("\n");
                            for (int k = 0; k < lines.length; k++) {
                                String lineText = lines[k].trim();
                                if (lineText.isEmpty())
                                    continue;
                                if (k % 2 != 0) {
                                    // Dòng 8
                                    content.append(
                                            "<div style='padding: 0 0px; margin-bottom: 5px; text-align: center;'>")
                                            .append(lineText).append("</div>");
                                } else {
                                    // Dòng 6
                                    content.append(
                                            "<div style='padding: 0 20px; margin-bottom: 5px; text-align: center;'>")
                                            .append(lineText).append("</div>");
                                }
                            }
                        }
                    }
                    foundContent = true;
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 24: " + e.getMessage());
            }
            
        
    }
}
