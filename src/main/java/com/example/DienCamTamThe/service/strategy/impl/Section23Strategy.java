package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section23Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 23;
    }

    @Override
    public String getSectionTitle() {
        return "Coi anh em";
    }

    @Override
    public void process(StringBuilder content, DivinationRequest request, String can, String chi, int ngaySinh, int thangSinh, String canChiGio, int thangThoThai, String mang, String cot, int truongSanhId, String gioSinhFull) {
        int canId = getCanId(can);
        int chiId = getChiId(chi);
        int mangId = getNguHanhId(mang);
        String mua = getMua(thangSinh);
        String buoi = getBuoi(gioSinhFull);
        boolean foundContent = false;
        

            if (truongSanhId <= 0) {
                content.append("<p><em>(Số này chưa tính được Vòng Trường Sinh - Không thể tra Sở 23)</em></p>");
            } else {
                try {
                    List<?> results = entityManager.createNativeQuery("SELECT * FROM so23_anhem WHERE TruongSanhID = ?")
                            .setParameter(1, truongSanhId).getResultList();

                    if (!results.isEmpty()) {
                        List<String> colNames = getTableColumnNames("so23_anhem");
                        int tenIdx = -1, thoIdx = -1;
                        for (int i = 0; i < colNames.size(); i++) {
                            String lower = colNames.get(i).toLowerCase();
                            if (lower.contains("ten") || lower.contains("sao"))
                                tenIdx = i;
                            if (lower.contains("baitho"))
                                thoIdx = i;
                        }

                        for (Object obj : results) {
                            Object[] row = (Object[]) obj;
                            String ten = (tenIdx >= 0 && row[tenIdx] != null) ? row[tenIdx].toString() : "";
                            String tho = (thoIdx >= 0 && row[thoIdx] != null) ? row[thoIdx].toString() : "";

                            content.append("<div style='margin-bottom:20px; text-align: left;'>");
                            content.append("<p style='text-align: center;'><strong>").append(ten)
                                    .append(":</strong></p>");

                            String[] lines = tho.split("\n");
                            for (int k = 0; k < lines.length; k++) {
                                String lineText = lines[k].trim();
                                if (lineText.isEmpty())
                                    continue;
                                if (k % 2 != 0) {
                                    // Dòng 8: Không thụt lề, căn giữa
                                    content.append(
                                            "<div style='padding: 0 0px; margin-bottom: 5px; text-align: center;'>")
                                            .append(lineText).append("</div>");
                                } else {
                                    // Dòng 6: Thụt lề 20px hai bên, căn giữa
                                    content.append(
                                            "<div style='padding: 0 20px; margin-bottom: 5px; text-align: center;'>")
                                            .append(lineText).append("</div>");
                                }
                            }
                            content.append("</div>");
                            foundContent = true;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi manual Sở 23: " + e.getMessage());
                }
            }
            
        
    }
}
