package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section11Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 11;
    }

    @Override
    public String getSectionTitle() {
        return "Coi Nghề nghiệp";
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
                List<String> s11Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so11%'").getResultList();
                if (s11Tables.isEmpty()) {
                    s11Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so_11%'").getResultList();
                }
                for (String tableName : s11Tables) {
                    try {
                        List<Object[]> colsInfo = entityManager
                                .createNativeQuery("SHOW COLUMNS FROM `" + tableName + "`").getResultList();
                        java.util.List<String> colNames = new java.util.ArrayList<>();
                        for (Object[] col : colsInfo)
                            colNames.add(col[0].toString().toLowerCase());

                        int nhColIdx = findColIndex(colNames, "nguhanhid", "ngu_hanhid", "mang", "mang_id");
                        int tsColIdx = findColIndex(colNames, "thangsanh", "thang_sanh", "thang");
                        int lgColIdx = findColIndex(colNames, "nhom_nghe", "nhomnghe", "loigiai", "ketqua", "noi_dung",
                                "mota");

                        String sql = "SELECT * FROM `" + tableName + "` WHERE `"
                                + colNames.get(nhColIdx >= 0 ? nhColIdx : 0) + "` = ? AND `"
                                + colNames.get(tsColIdx >= 0 ? tsColIdx : 1) + "` = ?";
                        var query = entityManager.createNativeQuery(sql);
                        query.setParameter(1, mangId);
                        query.setParameter(2, thangSinh);
                        List<?> rows = query.getResultList();

                        if (!rows.isEmpty()) {
                            content.append("<p><strong>Mạng ").append(mang).append(" sinh tháng ").append(thangSinh)
                                    .append("</strong></p>");
                            for (Object objRow : rows) {
                                Object[] row = (Object[]) objRow;
                                String loiGiaiVal = "N/A";
                                if (lgColIdx >= 0 && row[lgColIdx] != null) {
                                    loiGiaiVal = row[lgColIdx].toString().replace("\n", "<br>");
                                } else {
                                    // Fallback tìm text dài nhất
                                    int maxLen = -1;
                                    for (Object cell : row) {
                                        if (cell != null && cell.toString().length() > maxLen) {
                                            maxLen = cell.toString().length();
                                            loiGiaiVal = cell.toString().replace("\n", "<br>");
                                        }
                                    }
                                }
                                content.append("<div style='margin-bottom:8px;'>• ").append(loiGiaiVal)
                                        .append("</div>");
                            }
                            foundContent = true;
                            /* return block */
                        }
                    } catch (Exception eInner) {
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 11: " + e.getMessage());
            }
        
    }
}
