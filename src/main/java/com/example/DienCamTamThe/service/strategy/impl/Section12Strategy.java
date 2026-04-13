package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section12Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 12;
    }

    @Override
    public String getSectionTitle() {
        return "Coi Cốt con gì";
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
                List<String> s12Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so12%'").getResultList();
                if (s12Tables.isEmpty()) {
                    s12Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so_12%'").getResultList();
                }
                for (String tableName : s12Tables) {
                    try {
                        List<Object[]> colsInfo = entityManager
                                .createNativeQuery("SHOW COLUMNS FROM `" + tableName + "`").getResultList();
                        java.util.List<String> colNames = new java.util.ArrayList<>();
                        for (Object[] col : colsInfo)
                            colNames.add(col[0].toString().toLowerCase());

                        int chiColIdx = findColIndex(colNames, "chiid", "tuoi_chiid", "chi", "chi_id");
                        int tsColIdx = findColIndex(colNames, "thangsanh", "thang_sanh", "thang");
                        int cotColIdx = findColIndex(colNames, "tencot", "ten_cot", "cot", "ketqua");
                        int lgColIdx = findColIndex(colNames, "loigiai", "loi_giai", "noi_dung", "mota");

                        String sql = "SELECT * FROM `" + tableName + "` WHERE `"
                                + colNames.get(chiColIdx >= 0 ? chiColIdx : 0) + "` = ? AND `"
                                + colNames.get(tsColIdx >= 0 ? tsColIdx : 1) + "` = ?";
                        var query = entityManager.createNativeQuery(sql);
                        query.setParameter(1, chiId);
                        query.setParameter(2, thangSinh);
                        List<?> rows = query.getResultList();

                        if (!rows.isEmpty()) {
                            Object[] row = (Object[]) rows.get(0);
                            String tenCot = (cotColIdx >= 0 && row[cotColIdx] != null) ? row[cotColIdx].toString()
                                    : "N/A";
                            String loiGiaiVal = (lgColIdx >= 0 && row[lgColIdx] != null)
                                    ? row[lgColIdx].toString().replace("\n", "<br>")
                                    : "N/A";

                            content.append("<p><strong>Tuổi ").append(chi).append(" sinh tháng ").append(thangSinh)
                                    .append(": ").append(tenCot).append("</strong><br>")
                                    .append(loiGiaiVal).append("</p>");
                            foundContent = true;
                            /* return block */
                        }
                    } catch (Exception eInner) {
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 12: " + e.getMessage());
            }
        
    }
}
