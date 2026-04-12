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
        int mangId = getNguHanhId(mang);
        
        try {
            List<String> colNames = getTableColumnNames("so23_anhem");
            if (colNames.isEmpty()) {
                content.append("<p><em>(Bảng so23_anhem không tồn tại)</em></p>");
                return;
            }

            String mangCol = null;
            String thangCol = null;
            for (String col : colNames) {
                String c = col.toLowerCase();
                if (c.contains("nguhanhid") || c.contains("ngu_hanhid") || c.contains("mang_id") || c.equals("mang") || c.equals("ngu_hanh")) {
                    mangCol = col;
                }
                if (c.contains("thangsanh") || c.contains("thang_sanh") || c.equals("thang")) {
                    thangCol = col;
                }
            }
            
            if (mangCol == null || thangCol == null) {
                content.append("<p><em>(Bảng so23_anhem chưa được cấu trúc đúng với Mạng và Tháng Sinh)</em></p>");
                return;
            }

            String sql = "SELECT * FROM so23_anhem WHERE `" + mangCol + "` = ? AND `" + thangCol + "` = ?";
            List<?> results = entityManager.createNativeQuery(sql)
                    .setParameter(1, mangId)
                    .setParameter(2, thangSinh)
                    .getResultList();

            if (!results.isEmpty()) {
                int tenIdx = -1, thoIdx = -1;
                for (int i = 0; i < colNames.size(); i++) {
                    String lower = colNames.get(i).toLowerCase();
                    if (lower.contains("ten") || lower.contains("sao")) tenIdx = i;
                    if (lower.contains("baitho") || lower.contains("bai_tho") || lower.contains("loigiai")) thoIdx = i;
                }

                for (Object obj : results) {
                    Object[] row = (Object[]) obj;
                    String ten = (tenIdx >= 0 && row[tenIdx] != null) ? row[tenIdx].toString() : "Tên sao";
                    String tho = (thoIdx >= 0 && row[thoIdx] != null) ? row[thoIdx].toString() : "Bài thơ chưa cập nhật";

                    content.append("<p><strong>Mạng ").append(mapNguHanhToVietnamese(mang)).append(" sinh tháng ").append(thangSinh).append("</strong></p>");
                    content.append("<div style='margin-bottom:20px; text-align: left;'>");
                    content.append("<p style='text-align: center;'><strong>").append(ten).append(":</strong></p>");

                    String[] lines = tho.split("\n");
                    for (int k = 0; k < lines.length; k++) {
                        String lineText = lines[k].trim();
                        if (lineText.isEmpty()) continue;
                        if (k % 2 != 0) {
                            content.append("<div style='padding: 0 0px; margin-bottom: 5px; text-align: center;'>").append(lineText).append("</div>");
                        } else {
                            content.append("<div style='padding: 0 20px; margin-bottom: 5px; text-align: center;'>").append(lineText).append("</div>");
                        }
                    }
                    content.append("</div>");
                }
            } else {
                 content.append("<p><em>(Không tìm thấy dữ liệu anh em cho Mạng ").append(mapNguHanhToVietnamese(mang))
                        .append(" sinh tháng ").append(thangSinh).append(")</em></p>");
            }
        } catch (Exception e) {
            System.err.println("Lỗi manual Sở 23: " + e.getMessage());
            content.append("<p style='color:red;'>Lỗi tải bảng anh em: ").append(e.getMessage()).append("</p>");
        }
    }
}
