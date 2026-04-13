package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section34Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 34;
    }

    @Override
    public String getSectionTitle() {
        return "Mega section (Triết lý, Nhân quả, Ngũ tạng, Lời kết)";
    }

    @Override
    public void process(StringBuilder content, DivinationRequest request, String can, String chi, int ngaySinh, int thangSinh, String canChiGio, int thangThoThai, String mang, String cot, int truongSanhId, String gioSinhFull) {
        int canId = getCanId(can);
        int chiId = getChiId(chi);
        int mangId = getNguHanhId(mang);
        String mua = getMua(thangSinh);
        String buoi = getBuoi(gioSinhFull);
        boolean foundContent = false;
        

            // Because these are combined into section 34, we must "consume" 35, 36, 37
            // by appending a hidden marker so the generic logic doesn't run.
            if (getSectionNumber() != 34) {
                content.append("<!-- Handled in Sở 34 -->");
                /* return block */
            }

            try {
                // 1. Sở 34: Triết lý & Ngũ phương
                content.append(
                        "<h3 style='color: #d32f2f; text-align: center; border-bottom: 2px solid #d32f2f; padding-bottom: 5px;'>TRIẾT LÝ & NGŨ PHƯƠNG</h3>");
                List<Object[]> trietly = entityManager.createNativeQuery("SELECT TieuDe, NoiDung FROM so34_trietly")
                        .getResultList();
                for (Object[] row : trietly) {
                    content.append("<p><strong>").append(row[0]).append("</strong><br>")
                            .append(row[1].toString().replace("\n", "<br>")).append("</p>");
                }

                // Ngũ phương content
                List<Object[]> nguphuong = entityManager
                        .createNativeQuery("SELECT TenDe, NguHanh, MauSac FROM so34_nguphuong").getResultList();
                for (Object[] row : nguphuong) {
                    content.append("<p>• <strong>").append(row[0]).append("</strong>: ")
                            .append(row[1]).append(" (").append(row[2]).append(")</p>");
                }

                // 2. Sở 35: Nhân quả
                content.append(
                        "<div class='view-all-header' style='display: none; color: #8c1010; font-size: 1.3em; font-weight: bold; margin-top: 40px; border-bottom: 1px dashed #ccc; padding-bottom: 5px; text-align: center; font-family: serif; text-transform: uppercase;'>SỞ SỐ 35</div>");
                content.append(
                        "<h3 style='color: #d32f2f; text-align: center; border-bottom: 2px solid #d32f2f; padding-bottom: 5px; margin-top: 30px;'>NHÂN QUẢ</h3>");
                List<Object[]> nhanqua = entityManager
                        .createNativeQuery("SELECT BoPhan, HanhVi_Nhan, KetQua_Qua FROM so35_nhan_qua").getResultList();
                for (Object[] row : nhanqua) {
                    content.append("<p>• <strong>").append(row[0]).append("</strong>: ")
                            .append(row[1]).append(" &rarr; <em>").append(row[2]).append("</em></p>");
                }

                // 3. Sở 36: Ngũ tạng
                content.append(
                        "<div class='view-all-header' style='display: none; color: #8c1010; font-size: 1.3em; font-weight: bold; margin-top: 40px; border-bottom: 1px dashed #ccc; padding-bottom: 5px; text-align: center; font-family: serif; text-transform: uppercase;'>SỞ SỐ 36</div>");
                content.append(
                        "<h3 style='color: #d32f2f; text-align: center; border-bottom: 2px solid #d32f2f; padding-bottom: 5px; margin-top: 30px;'>NGŨ TẠNG</h3>");
                List<Object[]> ngutang = entityManager
                        .createNativeQuery("SELECT TangPhu, NguHanh, MoTa FROM so36_ngu_tang").getResultList();
                for (Object[] row : ngutang) {
                    content.append("<p><strong>").append(row[0]).append("</strong> (").append(row[1]).append(")<br>")
                            .append(row[2]).append("</p>");
                }

                // 4. Sở 37: Lời kết
                content.append(
                        "<div class='view-all-header' style='display: none; color: #8c1010; font-size: 1.3em; font-weight: bold; margin-top: 40px; border-bottom: 1px dashed #ccc; padding-bottom: 5px; text-align: center; font-family: serif; text-transform: uppercase;'>SỞ SỐ 37</div>");
                content.append(
                        "<h3 style='color: #d32f2f; text-align: center; border-bottom: 2px solid #d32f2f; padding-bottom: 5px; margin-top: 30px;'>LỜI KẾT</h3>");
                List<Object[]> loiket = entityManager.createNativeQuery("SELECT TieuDe, NoiDung FROM so37_loi_ket")
                        .getResultList();
                for (Object[] row : loiket) {
                    content.append("<p><strong>").append(row[0]).append("</strong><br>")
                            .append(row[1].toString().replace("\n", "<br>")).append("</p>");
                }

                foundContent = true;
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 34 mega: " + e.getMessage());
            }
            
        
    }
}
