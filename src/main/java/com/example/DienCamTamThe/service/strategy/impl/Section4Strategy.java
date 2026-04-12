package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import com.example.DienCamTamThe.service.strategy.DivinationStrategy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section4Strategy implements DivinationStrategy {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public int getSectionNumber() {
        return 4;
    }

    @Override
    public String getSectionTitle() {
        return "Coi tuổi Mạng (Ngũ hành)";
    }

    @Override
    public void process(StringBuilder content, DivinationRequest request, String can, String chi, int ngaySinh, int thangSinh, String canChiGio, int thangThoThai, String mang, String cot, int truongSanhId, String gioSinhFull) {
        try {
            int canId = getCanId(can);
            int chiId = getChiId(chi);
            
            String sql = "SELECT t.NguHanhID, t.ChiTietMang, n.Name FROM so04_tuoimang t " +
                    "LEFT JOIN nguhanh n ON t.NguHanhID = n.ID " +
                    "WHERE t.CanID = ? AND t.ChiID = ? LIMIT 1";
            List<?> results = entityManager.createNativeQuery(sql)
                    .setParameter(1, canId).setParameter(2, chiId)
                    .getResultList();

            if (!results.isEmpty()) {
                Object[] r = (Object[]) results.get(0);
                String nguHanhName = r[2] != null ? r[2].toString() : "N/A";
                String chiTiet = r[1] != null ? r[1].toString() : "";
                content.append("<p><strong>Mạng ").append(nguHanhName).append(":</strong> ").append(chiTiet)
                        .append("</p>");
            } else {
                 content.append("<p><em>(Nội dung cho Sở 4 đang được cập nhật trong Database...)</em></p>");
            }
        } catch (Exception e) {
            System.err.println("Lỗi Sở 4: " + e.getMessage());
        }
    }
    
    // Giả sử có helper getCanId / getChiId được tái sử dụng
    private int getCanId(String can) {
        can = can.toLowerCase();
        switch (can) {
            case "giáp": return 1;
            case "ất": return 2;
            case "bính": return 3;
            case "đinh": return 4;
            case "mậu": return 5;
            case "kỷ": return 6;
            case "canh": return 7;
            case "tân": return 8;
            case "nhâm": return 9;
            case "quý": return 10;
            default: return -1;
        }
    }

    private int getChiId(String chi) {
        chi = chi.toLowerCase();
        switch (chi) {
            case "tý": return 1;
            case "sửu": return 2;
            case "dần": return 3;
            case "mão": return 4;
            case "thìn": return 5;
            case "tỵ": return 6;
            case "ngọ": return 7;
            case "mùi": return 8;
            case "thân": return 9;
            case "dậu": return 10;
            case "tuất": return 11;
            case "hợi": return 12;
            default: return -1;
        }
    }
}
