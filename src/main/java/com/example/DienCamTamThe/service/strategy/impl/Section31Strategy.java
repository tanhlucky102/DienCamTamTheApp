package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section31Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 31;
    }

    @Override
    public String getSectionTitle() {
        return "Coi kết luận (Phần trăm)";
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
                String sql = "SELECT TieuSo, DaiSo, LoiNhac FROM so31_dinh_so_song_lau WHERE MangID = ? AND ThangSanh = ?";
                List<?> res = entityManager.createNativeQuery(sql)
                        .setParameter(1, mangId).setParameter(2, thangSinh).getResultList();

                if (!res.isEmpty()) {
                    Object[] row = (Object[]) res.get(0);
                    int tieuSo = (int) row[0];
                    int daiSo = (int) row[1];
                    String loiNhac = row[2] != null ? row[2].toString() : "";

                    content.append("<p><strong>• Mạng ").append(mapNguHanhToVietnamese(mang)).append(" sinh tháng ")
                            .append(thangSinh).append("</strong><br>");
                    content.append("Tiểu số: ").append(tieuSo).append("<br>");
                    content.append("Đại số: ").append(daiSo).append("</p>");
                    content.append("<p><strong>• Lời Nhắc</strong><br>").append(loiNhac.replace("\n", "<br>"))
                            .append("</p>");

                    foundContent = true;
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 31: " + e.getMessage());
            }
            
        
    }
}
