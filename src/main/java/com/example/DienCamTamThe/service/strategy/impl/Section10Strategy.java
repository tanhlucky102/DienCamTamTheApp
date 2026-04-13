package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section10Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 10;
    }

    @Override
    public String getSectionTitle() {
        return "Coi Thọ thai sanh (Cung mệnh)";
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
                List<String> s10Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so10%'").getResultList();
                if (s10Tables.isEmpty()) {
                    s10Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so_10%'").getResultList();
                }

                for (String tableName : s10Tables) {
                    try {
                        List<Object[]> colsInfo = entityManager
                                .createNativeQuery("SHOW COLUMNS FROM `" + tableName + "`").getResultList();
                        java.util.List<String> colNames = new java.util.ArrayList<>();
                        for (Object[] col : colsInfo)
                            colNames.add(col[0].toString().toLowerCase());

                        // Tìm cột Tháng Thọ Thai (ưu tiên các từ khóa liên quan thọ thai)
                        int thoColIdx = findColIndex(colNames, "thang_tho_thai", "thothai", "tho_thai");
                        // Tìm cột Tháng Sanh (ưu tiên các từ khóa liên quan sanh)
                        int sanhColIdx = findColIndex(colNames, "thang_sanh", "thangsanh", "sanh_thang", "thang");
                        // Cột Lời Giải
                        int lgColIdx = findColIndex(colNames, "loigiai", "loi_giai", "ketqua", "noi_dung", "mota");

                        if (thoColIdx < 0 && sanhColIdx < 0)
                            continue; // Không phải bảng chuẩn

                        String sql = "SELECT * FROM `" + tableName + "` WHERE 1=1 ";
                        List<Object> params = new java.util.ArrayList<>();
                        if (thoColIdx >= 0) {
                            sql += " AND `" + colNames.get(thoColIdx) + "` = ? ";
                            params.add(thangThoThai);
                        }
                        if (sanhColIdx >= 0) {
                            sql += " AND `" + colNames.get(sanhColIdx) + "` = ? ";
                            params.add(thangSinh);
                        }

                        var query = entityManager.createNativeQuery(sql);
                        for (int i = 0; i < params.size(); i++)
                            query.setParameter(i + 1, params.get(i));
                        List<?> rows = query.getResultList();

                        if (!rows.isEmpty()) {
                            Object[] row = (Object[]) rows.get(0);
                            String loiGiaiVal = "N/A";
                            if (lgColIdx >= 0 && row[lgColIdx] != null) {
                                loiGiaiVal = row[lgColIdx].toString().replace("\n", "<br>");
                            } else {
                                // Fallback: tìm text dài nhất
                                int maxLen = -1;
                                for (Object cell : row) {
                                    if (cell != null && cell.toString().length() > maxLen) {
                                        maxLen = cell.toString().length();
                                        loiGiaiVal = cell.toString().replace("\n", "<br>");
                                    }
                                }
                            }

                            content.append("<p><strong>Thọ thai tháng ").append(thangThoThai).append(" sanh tháng ")
                                    .append(thangSinh)
                                    .append("</strong><br>").append(loiGiaiVal).append("</p>");
                            foundContent = true;
                            /* return block */ // Dừng lại ở bảng đầu tiên có dữ liệu
                        }
                    } catch (Exception eInner) {
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 10: " + e.getMessage());
            }
        
    }
}
