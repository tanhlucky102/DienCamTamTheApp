package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section27Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 27;
    }

    @Override
    public String getSectionTitle() {
        return "Coi ngày kỵ hạp";
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
                String genderVn = getGenderVn(request.getGender());

                // 1. so27_tongquan
                List<?> res1 = entityManager.createNativeQuery(
                        "SELECT Tuoi_CanID, Tuoi_ChiID, Mang, Tho FROM so27_tongquan WHERE Tuoi_CanID = ? AND Tuoi_ChiID = ? AND GioiTinh = ?")
                        .setParameter(1, canId).setParameter(2, chiId).setParameter(3, genderVn).getResultList();

                for (Object obj : res1) {
                    Object[] row = (Object[]) obj;
                    String cName = getCanName((int) row[0]);
                    String chName = getChiName((int) row[1]);
                    String mVal = row[2] != null ? row[2].toString() : "";
                    String tVal = row[3] != null ? row[3].toString() : "";

                    content.append("<p style='line-height:1.6;'>")
                            .append("<span style='color:#d32f2f; font-weight:bold;'>Tuổi: </span>").append(cName)
                            .append(" ").append(chName).append("<br>")
                            .append("<span style='color:#1a5fb4; font-weight:bold;'>Mạng: </span>").append(mVal)
                            .append("<br>")
                            .append("<span style='color:#2e7d32; font-weight:bold;'>Thơ: </span>")
                            .append("</p>");

                    if (!tVal.isEmpty()) {
                        String[] lines = tVal.split("\n");
                        content.append("<div style='margin-bottom: 20px;'>");
                        for (int k = 0; k < lines.length; k++) {
                            String lt = lines[k].trim();
                            if (lt.isEmpty())
                                continue;
                            int wordCount = lt.split("\\s+").length;
                            if (wordCount >= 7) {
                                // Dòng 8 chữ: padding 0px
                                content.append(
                                        "<div style='padding: 0 0px; margin-bottom: 5px; text-align: center; font-style: italic;'>")
                                        .append(lt).append("</div>");
                            } else {
                                // Dòng 6 chữ: padding 20px
                                content.append(
                                        "<div style='padding: 0 20px; margin-bottom: 5px; text-align: center; font-style: italic;'>")
                                        .append(lt).append("</div>");
                            }
                        }
                        content.append("</div>");
                    }
                    foundContent = true;
                }

                // 2. so27_thangkyhap
                List<?> res2 = entityManager.createNativeQuery(
                        "SELECT TuoiApDung, DieuHap, DieuKy FROM so27_thangkyhap WHERE Tuoi_CanID = ? AND Tuoi_ChiID = ? AND GioiTinh = ? AND ThangSanh = ?")
                        .setParameter(1, canId).setParameter(2, chiId).setParameter(3, genderVn)
                        .setParameter(4, thangSinh).getResultList();
                for (Object obj : res2) {
                    Object[] row = (Object[]) obj;
                    content.append(
                            "<div style='margin-top:15px; background: rgba(26, 95, 180, 0.05); padding: 10px; border-radius: 8px;'>")
                            .append("<p style='margin:0;'>")
                            .append("<strong>Tuổi áp dụng: </strong>").append(row[0] != null ? row[0].toString() : "")
                            .append("<br>")
                            .append("<strong>Điều Hạp: </strong>").append(row[1] != null ? row[1].toString() : "")
                            .append("<br>")
                            .append("<strong>Điều Kỵ: </strong>").append(row[2] != null ? row[2].toString() : "")
                            .append("</p></div>");
                    foundContent = true;
                }

                // 3. so27_ngaykyhap - Lấy tất cả LoiGiai theo Tuoi_CanID và Tuoi_ChiID
                try {
                    List<?> res3 = entityManager.createNativeQuery(
                            "SELECT LoiGiai FROM so27_ngaykyhap WHERE Tuoi_CanID = ? AND Tuoi_ChiID = ?")
                            .setParameter(1, canId).setParameter(2, chiId).getResultList();
                    if (!res3.isEmpty()) {
                        String canName = getCanName(canId);
                        String chiName = getChiName(chiId);
                        content.append("<h6 style='color:#d32f2f; margin-top:20px; margin-bottom:8px; font-size:1em;'>")
                                .append("Ngày kỵ hạp của tuổi ").append(canName).append(" ").append(chiName)
                                .append("</h6>");
                        for (Object obj : res3) {
                            content.append(
                                    "<div style='margin-top:10px; border-left: 4px solid #d32f2f; padding-left: 10px;'>")
                                    .append("<p style='margin:0;'>").append(obj.toString().replace("\n", "<br>"))
                                    .append("</p></div>");
                            foundContent = true;
                        }
                    }
                } catch (Exception eDay) {
                    System.err.println("Lỗi query so27_ngaykyhap: " + eDay.getMessage());
                }

            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 27: " + e.getMessage());
            }
            
        
    }
}
