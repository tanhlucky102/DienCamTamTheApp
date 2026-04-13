package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section22Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 22;
    }

    @Override
    public String getSectionTitle() {
        return "Coi nuôi con";
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
                content.append("<p><em>(Số này chưa tính được Vòng Trường Sinh - Không thể tra Sở 22)</em></p>");
            } else {
                try {
                    List<?> results = entityManager
                            .createNativeQuery("SELECT * FROM so22_nuoicon WHERE TruongSanhID = ?")
                            .setParameter(1, truongSanhId).getResultList();

                    if (!results.isEmpty()) {
                        List<String> colNames = getTableColumnNames("so22_nuoicon");
                        int tenIdx = -1, lgIdx = -1;
                        for (int i = 0; i < colNames.size(); i++) {
                            String lower = colNames.get(i).toLowerCase();
                            if (lower.contains("ten") || lower.contains("sao"))
                                tenIdx = i;
                            if (lower.contains("loigiai"))
                                lgIdx = i;
                        }

                        for (Object obj : results) {
                            Object[] row = (Object[]) obj;
                            String ten = (tenIdx >= 0 && row[tenIdx] != null) ? row[tenIdx].toString() : "";
                            String lg = (lgIdx >= 0 && row[lgIdx] != null) ? row[lgIdx].toString() : "";

                            content.append("<p><strong>").append(ten).append("</strong><br>")
                                    .append(lg.replace("\n", "<br>")).append("</p>");
                            foundContent = true;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi manual Sở 22: " + e.getMessage());
                }
            }
            
        
    }
}
