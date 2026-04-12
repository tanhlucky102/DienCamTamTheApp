package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section17Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 17;
    }

    @Override
    public String getSectionTitle() {
        return "Coi thi cử (Kỳ nhất, kỳ nhì)";
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
                // Tiêu đề Kỳ nhất
                content.append("<p><strong>Thi cử kỳ nhất</strong></p>");

                // Query Kỳ nhất (dùng SELECT * và tìm cột động để tránh sai sót)
                List<?> res1 = entityManager
                        .createNativeQuery("SELECT * FROM so17_thicu_kynhat WHERE ChiID = ? AND ThangSanh = ?")
                        .setParameter(1, chiId).setParameter(2, thangSinh).getResultList();

                if (res1.isEmpty()) {
                    content.append("<p>Số này không có luận giải cho kỳ thi này.</p>");
                } else {
                    List<String> cols1 = getTableColumnNames("so17_thicu_kynhat");
                    int tenIdx = -1, lgIdx = -1;
                    for (int i = 0; i < cols1.size(); i++) {
                        String c = cols1.get(i).toLowerCase();
                        if (c.contains("ten") || c.contains("tu"))
                            tenIdx = i;
                        if (c.contains("loigiai"))
                            lgIdx = i;
                    }
                    for (Object obj : res1) {
                        Object[] row = (Object[]) obj;
                        String ten = (tenIdx >= 0 && row[tenIdx] != null) ? row[tenIdx].toString() : "";
                        String lg = (lgIdx >= 0 && row[lgIdx] != null) ? row[lgIdx].toString() : "";
                        content.append("<p><b>").append(ten).append("</b> ").append(lg.replace("\n", "<br>"))
                                .append("</p>");
                    }
                }

                // Tiêu đề Kỳ nhì
                content.append("<p><strong>Thi cử kỳ nhì</strong></p>");

                // Query Kỳ nhì
                List<?> res2 = entityManager
                        .createNativeQuery("SELECT * FROM so17_thicu_kynhi WHERE ChiID = ? AND ThangSanh = ?")
                        .setParameter(1, chiId).setParameter(2, thangSinh).getResultList();

                if (res2.isEmpty()) {
                    content.append("<p>Số này không có luận giải cho kỳ thi này.</p>");
                } else {
                    List<String> cols2 = getTableColumnNames("so17_thicu_kynhi");
                    int tenIdx2 = -1, lgIdx2 = -1;
                    for (int i = 0; i < cols2.size(); i++) {
                        String c = cols2.get(i).toLowerCase();
                        if (c.contains("ten") || c.contains("ketqua"))
                            tenIdx2 = i;
                        if (c.contains("loigiai"))
                            lgIdx2 = i;
                    }
                    for (Object obj : res2) {
                        Object[] row = (Object[]) obj;
                        String ten = (tenIdx2 >= 0 && row[tenIdx2] != null) ? row[tenIdx2].toString() : "";
                        String lg = (lgIdx2 >= 0 && row[lgIdx2] != null) ? row[lgIdx2].toString() : "";
                        content.append("<p><b>").append(ten).append("</b> ").append(lg.replace("\n", "<br>"))
                                .append("</p>");
                    }
                }

                foundContent = true;
                /* return block */
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 17: " + e.getMessage());
            }
        
    }
}
