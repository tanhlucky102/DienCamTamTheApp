package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section6Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 6;
    }

    @Override
    public String getSectionTitle() {
        return "Coi Tam Thế (Tháng/Giờ/Tuổi/Mạng)";
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
                // 1. Mạng mới sanh
                String sql1 = "SELECT n.Name, m.LoiGiai FROM so06_moi_sanh_mang_gi m JOIN nguhanh n ON m.NguHanhID = n.ID WHERE m.NguHanhID = ? LIMIT 1";
                List<?> res1 = entityManager.createNativeQuery(sql1).setParameter(1, mangId).getResultList();
                if (!res1.isEmpty()) {
                    Object[] r = (Object[]) res1.get(0);
                    content.append("<p><strong>Mạng ").append(r[0]).append(":</strong> ").append(r[1]).append("</p>");
                    foundContent = true;
                }

                int gioId = getChiId(canChiGio);
                java.util.Map<String, String> khMatches = new java.util.LinkedHashMap<>();
                java.util.Set<String> allNoiDunSet = new java.util.LinkedHashSet<>();

                // Tập hợp tất cả các NoiDung có thể có trong Sở 6
                String[] allKHTables = {
                        "so06_kiet_hung_can_gio", "so06_kiet_hung_mang_thang",
                        "so06_kiet_hung_nam_gio", "so06_kiet_hung_thang_gio",
                        "so06_kiet_hung_tuoi_thang"
                };

                for (String table : allKHTables) {
                    try {
                        List<?> allND = entityManager.createNativeQuery("SELECT DISTINCT NoiDung FROM " + table)
                                .getResultList();
                        for (Object nd : allND)
                            if (nd != null)
                                allNoiDunSet.add(nd.toString());
                    } catch (Exception e) {
                    }
                }

                // Thực hiện các query thực tế để tìm match
                try {
                    // Can + Giờ
                    List<?> res2 = entityManager
                            .createNativeQuery(
                                    "SELECT NoiDung, LoiGiai FROM so06_kiet_hung_can_gio WHERE CanID = ? AND GioID = ?")
                            .setParameter(1, canId).setParameter(2, gioId).getResultList();
                    for (Object obj : res2) {
                        Object[] r = (Object[]) obj;
                        khMatches.put(r[0].toString(), r[1].toString());
                    }

                    // Mạng + Tháng
                    List<?> res3 = entityManager.createNativeQuery(
                            "SELECT NoiDung, LoiGiai FROM so06_kiet_hung_mang_thang WHERE Mang = ? AND Thang = ?")
                            .setParameter(1, mangId).setParameter(2, thangSinh).getResultList();
                    for (Object obj : res3) {
                        Object[] r = (Object[]) obj;
                        khMatches.put(r[0].toString(), r[1].toString());
                    }

                    // Năm + Giờ
                    List<?> res4 = entityManager
                            .createNativeQuery(
                                    "SELECT NoiDung, LoiGiai FROM so06_kiet_hung_nam_gio WHERE ChiID = ? AND GioID = ?")
                            .setParameter(1, chiId).setParameter(2, gioId).getResultList();
                    for (Object obj : res4) {
                        Object[] r = (Object[]) obj;
                        khMatches.put(r[0].toString(), r[1].toString());
                    }

                    // Tháng + Giờ
                    List<?> res5 = entityManager.createNativeQuery(
                            "SELECT NoiDung, LoiGiai FROM so06_kiet_hung_thang_gio WHERE Thangsinh = ? AND GioID = ?")
                            .setParameter(1, thangSinh).setParameter(2, gioId).getResultList();
                    for (Object obj : res5) {
                        Object[] r = (Object[]) obj;
                        khMatches.put(r[0].toString(), r[1].toString());
                    }

                    // Tuổi + Tháng
                    List<?> res6 = entityManager.createNativeQuery(
                            "SELECT NoiDung, LoiGiai FROM so06_kiet_hung_tuoi_thang WHERE TuoiID = ? AND Thang = ?")
                            .setParameter(1, chiId).setParameter(2, thangSinh).getResultList();
                    for (Object obj : res6) {
                        Object[] r = (Object[]) obj;
                        khMatches.put(r[0].toString(), r[1].toString());
                    }
                } catch (Exception e) {
                }

                // Xuất kết quả: Có hoặc Không cho tất cả NoiDung dưới dạng danh sách gạch đầu
                // dòng
                content.append("<ul style='list-style-type: none; padding-left: 0;'>");
                for (String nd : allNoiDunSet) {
                    content.append("<li style='margin-bottom: 8px;'>");
                    content.append("• <b>").append(nd).append("</b>: ");
                    if (khMatches.containsKey(nd)) {
                        content.append("<span style='color: #2e7d32; font-weight: bold;'>Có</span> - ")
                                .append(khMatches.get(nd));
                    } else {
                        content.append("<span style='color: #d32f2f; font-weight: bold;'>Không</span>");
                    }
                    content.append("</li>");
                    foundContent = true;
                }
                content.append("</ul>");

            } catch (Exception e) {
                System.err.println("Lỗi Sở 6: " + e.getMessage());
            }
            
        
    }
}
