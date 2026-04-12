package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section5Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 5;
    }

    @Override
    public String getSectionTitle() {
        return "Coi Tâm giờ sanh (Ngày đêm)";
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
                // Chuẩn hóa giờ sinh sang định dạng HH:mm:00 để so sánh chuỗi
                String searchTime = gioSinhFull + ":00";

                String sql = "SELECT tu_gio, den_gio, ten_gio_chi, buoi FROM so05_tamgiosanh WHERE thang_sanh = ?";
                List<?> results = entityManager.createNativeQuery(sql)
                        .setParameter(1, thangSinh)
                        .getResultList();

                content.append(
                        "<p>Dựa trên tháng sinh âm lịch và giờ sinh, chúng tôi xác định cung giờ chính xác của bạn:</p>");
                for (Object rawRow : results) {
                    Object[] r = (Object[]) rawRow;
                    String tuGio = r[0].toString();
                    String denGio = r[1].toString();
                    String tenGioChi = r[2].toString();
                    String buoiVn = r[3].toString();

                    boolean match = false;
                    if (tuGio.compareTo(denGio) <= 0) {
                        // Khoảng trong ngày (VD: 04:00 - 06:00)
                        if (searchTime.compareTo(tuGio) >= 0 && searchTime.compareTo(denGio) <= 0)
                            match = true;
                    } else {
                        // Khoảng qua đêm (VD: 22:00 - 02:00)
                        if (searchTime.compareTo(tuGio) >= 0 || searchTime.compareTo(denGio) <= 0)
                            match = true;
                    }

                    if (match) {
                        content.append("<p><strong>Kết quả:</strong> Bạn sanh vào <b>Giờ ").append(tenGioChi)
                                .append("</b>");
                        content.append(" (Khoảng ").append(tuGio.substring(0, 5)).append(" - ")
                                .append(denGio.substring(0, 5));
                        content.append(" thuộc buổi ").append(buoiVn.equals("Ngay") ? "Ngày" : "Đêm").append(").</p>");
                        foundContent = true;
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi Sở 5: " + e.getMessage());
            }
            
        
    }
}
