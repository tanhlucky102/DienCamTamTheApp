package com.example.DienCamTamThe.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class DatabaseTestController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/api/test-db")
    public String testDatabaseCoverage() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset='UTF-8'><style>");
        sb.append("body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }");
        sb.append(".missing { color: red; font-weight: bold; }");
        sb.append(".warning { color: orange; font-weight: bold; }");
        sb.append(".ok { color: green; font-weight: bold; }");
        sb.append(".table-info { margin-left: 20px; color: #555; }");
        sb.append("</style></head><body>");
        sb.append("<h1>Báo cáo Kiểm tra Database Diễn Cầm Tam Thế (Sở 4-37)</h1>");
        sb.append("<hr/>");

        int missingCount = 0;
        int okCount = 0;

        for (int secNo = 4; secNo <= 37; secNo++) {
            String prefix1 = String.format("so%02d", secNo);
            String prefix2 = String.format("so%d\\_", secNo);
            
            List<String> tables = new ArrayList<>();
            try {
                List<?> rows = entityManager.createNativeQuery("SHOW TABLES LIKE '" + prefix1 + "%'").getResultList();
                for (Object row : rows) tables.add(row.toString());
                
                if (tables.isEmpty() && secNo < 10) {
                     List<?> rows2 = entityManager.createNativeQuery("SHOW TABLES LIKE '" + prefix2 + "%'").getResultList();
                     for (Object row : rows2) tables.add(row.toString());
                }
            } catch (Exception e) {
                sb.append("<p>Lỗi kết nối MySQL: ").append(e.getMessage()).append("</p>");
                break;
            }

            if (tables.isEmpty()) {
                sb.append("<p><strong>Quẻ Sở ").append(secNo).append(":</strong> <span class='missing'>[THIẾU DATA] Không tìm thấy bảng nào!</span></p>");
                missingCount++;
            } else {
                sb.append("<p><strong>Quẻ Sở ").append(secNo).append(":</strong> <span class='ok'>[OK] Tìm thấy ").append(tables.size()).append(" bảng.</span></p>");
                for (String table : tables) {
                    sb.append("<div class='table-info'>- Bảng <code>").append(table).append("</code>: ");
                    try {
                        List<Object[]> cols = entityManager.createNativeQuery("SHOW COLUMNS FROM `" + table + "`").getResultList();
                        List<String> colNames = new ArrayList<>();
                        for (Object[] colObj : cols) colNames.add(colObj[0].toString());
                        sb.append("Cột [").append(String.join(", ", colNames)).append("]");
                    } catch (Exception e) {
                        sb.append("<span class='warning'>(Lỗi quét cột)</span>");
                    }
                    sb.append("</div>");
                }
                okCount++;
            }
        }

        sb.append("<hr/>");
        sb.append("<h3>TỔNG KẾT:</h3>");
        sb.append("<ul><li>Hoàn thiện: <strong>").append(okCount).append("/34 Sở</strong></li>");
        sb.append("<li>Cần thêm bảng/data: <strong class='missing'>").append(missingCount).append(" Sở</strong></li></ul>");
        sb.append("</body></html>");

        return sb.toString();
    }
}
