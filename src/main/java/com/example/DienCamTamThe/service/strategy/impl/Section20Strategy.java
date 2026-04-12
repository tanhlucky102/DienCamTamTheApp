package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section20Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 20;
    }

    @Override
    public String getSectionTitle() {
        return "Coi duyên nợ vợ chồng";
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
                // Bước 1: Tra ma trận duyên nợ (dùng NguHanhID của chồng + ThangSanh của vợ)
                String sql1 = "SELECT KetQua FROM so20_ma_tran_duyen_no WHERE (Chong_NguHanhID = ? OR chong_ngu_hanhid = ?) AND (Vo_ThangSanh = ? OR vo_thang_sanh = ?) LIMIT 1";
                List<?> kqList = entityManager.createNativeQuery(sql1)
                        .setParameter(1, mangId).setParameter(2, mangId)
                        .setParameter(3, thangSinh).setParameter(4, thangSinh)
                        .getResultList();
                if (!kqList.isEmpty()) {
                    String ketQua = kqList.get(0).toString();
                    // Bước 2: Tra lời giải từ KetQua
                    String sql2 = "SELECT LoiGiai FROM so20_loi_giai_duyen_no WHERE (KetQua = ? OR ket_qua = ?) LIMIT 1";
                    List<?> lgList = entityManager.createNativeQuery(sql2)
                            .setParameter(1, ketQua).setParameter(2, ketQua)
                            .getResultList();
                    content.append("<div style='margin-bottom: 10px;'><h6 style='color:#b8860b'>▶ Duyên nợ: ")
                            .append(ketQua).append("</h6>");
                    for (Object lg : lgList) {
                        if (lg != null) {
                            content.append("<p>").append(lg.toString().replace("\n", "<br>")).append("</p>");
                        }
                    }
                    content.append("</div>");
                    foundContent = true;
                }

                // Tháng xung khắc
                String sql3 = "SELECT ThangPham FROM so20_thang_xungkhac WHERE ChiID = ? AND GioiTinh = ? LIMIT 1";
                String genderVn = getGenderVn(request.getGender());
                List<?> xkList = entityManager.createNativeQuery(sql3)
                        .setParameter(1, chiId).setParameter(2, genderVn)
                        .getResultList();
                if (!xkList.isEmpty() && xkList.get(0) != null) {
                    content.append("<p><strong>Tháng xung khắc:</strong> ").append(xkList.get(0).toString())
                            .append("</p>");
                    foundContent = true;
                }
            } catch (Exception e) {
                System.err.println("Lỗi Sở 20: " + e.getMessage());
            }
            if (!foundContent) {
                content.append(
                        "<p><em>(Chúc mừng bạn, theo sách Diễn Cầm Tam Thế, tuổi và tháng sinh của bạn không phạm phải tai ương này)</em></p>");
            }
            /* return block */
        
    }
}
