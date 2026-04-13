package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section9Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 9;
    }

    @Override
    public String getSectionTitle() {
        return "Coi Ngày sanh";
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
                List<String> s9Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so09%'").getResultList();
                if (s9Tables.isEmpty()) {
                    s9Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so_09%'").getResultList();
                }
                for (String tableName : s9Tables) {
                    try {
                        List<Object[]> colsInfo = entityManager
                                .createNativeQuery("SHOW COLUMNS FROM `" + tableName + "`").getResultList();
                        java.util.List<String> colNames = new java.util.ArrayList<>();
                        for (Object[] col : colsInfo)
                            colNames.add(col[0].toString().toLowerCase());

                        int dayColIdx = findColIndex(colNames, "ngay", "ngaysanh", "ngay_sanh", "ngay_id");
                        int starColIdx = findColIndex(colNames, "tensao", "ten_sao", "hieu_ngay", "sao", "tieu_de");
                        int lgColIdx = findColIndex(colNames, "loigiai", "loi_giai", "ketqua", "noi_dung", "mota");

                        String sql = "SELECT * FROM `" + tableName + "` WHERE `"
                                + colNames.get(dayColIdx >= 0 ? dayColIdx : 0) + "` = ?";
                        var query = entityManager.createNativeQuery(sql);
                        query.setParameter(1, ngaySinh);
                        List<?> rows = query.getResultList();

                        if (!rows.isEmpty()) {
                            Object[] row = (Object[]) rows.get(0);
                            String tenSao = (starColIdx >= 0 && row[starColIdx] != null) ? row[starColIdx].toString()
                                    : "N/A";
                            String loiGiaiVal = (lgColIdx >= 0 && row[lgColIdx] != null) ? row[lgColIdx].toString()
                                    : "N/A";

                            content.append("<p><strong>Ngày ").append(ngaySinh).append(": ").append(tenSao)
                                    .append("</strong><br>").append(loiGiaiVal).append("</p>");
                            foundContent = true;
                            /* return block */
                        }
                    } catch (Exception eInner) {
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 9: " + e.getMessage());
            }
        
    }
}
