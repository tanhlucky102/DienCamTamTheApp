package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section8Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 8;
    }

    @Override
    public String getSectionTitle() {
        return "Coi 36 giờ sanh";
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
                String giaiDoan = getGiaiDoanGio(gioSinhFull);
                String chiGio = canChiGio;

                // Tìm TẤT CẢ bảng có tên bắt đầu bằng so08
                List<String> s8Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so08%'").getResultList();
                if (s8Tables.isEmpty()) {
                    // Thử prefix khác
                    s8Tables = entityManager.createNativeQuery("SHOW TABLES LIKE 'so_08%'").getResultList();
                }

                for (String tableName : s8Tables) {
                    try {
                        List<Object[]> colsInfo = entityManager
                                .createNativeQuery("SHOW COLUMNS FROM `" + tableName + "`").getResultList();
                        java.util.List<String> colNames = new java.util.ArrayList<>();
                        for (Object[] col : colsInfo)
                            colNames.add(col[0].toString().toLowerCase());

                        int chiColIdx = findColIndex(colNames, "chiid", "chi_id", "gio_id", "ten_gio_chi",
                                "tuoi_chiid");
                        int gdColIdx = findColIndex(colNames, "giaidoan", "giai_doan", "phan", "segment", "buoi_sanh");
                        int lgColIdx = findColIndex(colNames, "loigiai", "loi_giai", "ketqua", "noi_dung", "mota");

                        // Xây dựng query filter theo Chi và GiaiDoan
                        StringBuilder sql = new StringBuilder("SELECT * FROM `").append(tableName)
                                .append("` WHERE 1=1 ");
                        List<Object> params = new java.util.ArrayList<>();

                        if (chiColIdx >= 0) {
                            sql.append(" AND `").append(colNames.get(chiColIdx)).append("` = ? ");
                            params.add(getChiId(chiGio));
                        }
                        // Nếu có cột GiaiDoan, lọc đúng segment
                        if (gdColIdx >= 0) {
                            sql.append(" AND (`").append(colNames.get(gdColIdx)).append("` LIKE ? OR `")
                                    .append(colNames.get(gdColIdx)).append("` LIKE ? ) ");
                            params.add("%" + giaiDoan + "%");
                            params.add("%" + (giaiDoan.equals("Sau") ? "Cuối" : giaiDoan) + "%");
                        }

                        var query = entityManager.createNativeQuery(sql.toString());
                        for (int i = 0; i < params.size(); i++)
                            query.setParameter(i + 1, params.get(i));
                        List<?> rows = query.getResultList();

                        if (!rows.isEmpty()) {
                            // Nếu có nhiều hơn 1 row (do like filter), ưu tiên row khớp nhất hoặc lấy row
                            // đầu
                            Object[] row = (Object[]) rows.get(0);
                            String loiGiaiVal = "N/A";
                            if (lgColIdx >= 0 && row[lgColIdx] != null) {
                                loiGiaiVal = row[lgColIdx].toString().replace("\n", "<br>");
                            } else {
                                // Nếu không tìm thấy cột lời giải đích danh, lấy cột text dài nhất
                                int maxLen = -1;
                                for (Object cell : row) {
                                    if (cell != null && cell.toString().length() > maxLen) {
                                        maxLen = cell.toString().length();
                                        loiGiaiVal = cell.toString().replace("\n", "<br>");
                                    }
                                }
                            }

                            content.append("<p><strong>").append(giaiDoan).append(" giờ ").append(chiGio)
                                    .append("</strong><br>").append(loiGiaiVal).append("</p>");
                            foundContent = true;
                            // Quan trọng: return để KHÔNG chạy logic dynamic scanner phía dưới
                            /* return block */
                        }
                    } catch (Exception eInner) {
                        System.err.println("Lỗi query bảng " + tableName + ": " + eInner.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 8: " + e.getMessage());
            }
        
    }
}
