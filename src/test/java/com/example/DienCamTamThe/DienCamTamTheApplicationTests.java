package com.example.DienCamTamThe;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class DienCamTamTheApplicationTests {

    @Autowired
    private EntityManager entityManager;

    @Test
    void scanMissingTables() {
        System.out.println("====== BẮT ĐẦU QUÉT DATABASE (1-34) ======");
        for (int secNo = 1; secNo <= 34; secNo++) {
            String prefix = String.format("so%02d", secNo);
            boolean found = false;
            try {
                List<?> rows = entityManager.createNativeQuery("SHOW TABLES LIKE '" + prefix + "%'").getResultList();
                if (!rows.isEmpty()) {
                    found = true;
                    System.out.print("[OK] Sở " + secNo + " có các bảng: ");
                    for(Object row : rows) System.out.print(row.toString() + " ");
                    System.out.println();
                } else if (secNo < 10) {
                     String prefix2 = String.format("so%d", secNo);
                     rows = entityManager.createNativeQuery("SHOW TABLES LIKE '" + prefix2 + "%'").getResultList();
                     if (!rows.isEmpty()) {
                         found = true;
                         System.out.print("[OK] Sở " + secNo + " có các bảng (soX): ");
                         for(Object row : rows) System.out.print(row.toString() + " ");
                         System.out.println();
                     }
                }
                
                if (!found) {
                    System.out.println("[MISSING] THIẾU DỮ LIỆU SỞ: " + secNo);
                }
            } catch (Exception e) {
                System.out.println("Lỗi SQL: " + e.getMessage());
            }
        }
        System.out.println("====== KẾT THÚC QUÉT ======");
    }
}
