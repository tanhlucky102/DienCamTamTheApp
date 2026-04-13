package com.example.DienCamTamThe.service.strategy.impl;

import com.example.DienCamTamThe.entity.DivinationRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Section7Strategy extends AbstractDivinationStrategy {

    @Override
    public int getSectionNumber() {
        return 7;
    }

    @Override
    public String getSectionTitle() {
        return "Coi Hồn đầu thai";
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
                String genderVn = getGenderVn(request.getGender());
                String[] possibleTables = { "so07_hon_dau_thai", "so07_hondauthai", "so07_hon_dau_thai_12_cau" };

                for (String tableName : possibleTables) {
                    try {
                        List<?> allRows = entityManager.createNativeQuery("SELECT * FROM " + tableName).getResultList();
                        if (allRows.isEmpty())
                            continue;

                        List<Object[]> colsInfo = entityManager.createNativeQuery("SHOW COLUMNS FROM " + tableName)
                                .getResultList();
                        java.util.List<String> colNames = new java.util.ArrayList<>();
                        for (Object[] col : colsInfo)
                            colNames.add(col[0].toString().toLowerCase());

                        int nhIdx = findColIndex(colNames, "nguhanhid", "ngu_hanhid", "mang_id");
                        int tsIdx = findColIndex(colNames, "thangsanh", "thang_sanh", "thang");
                        int gtIdx = findColIndex(colNames, "gioitinh", "gioi_tinh", "phai", "nam_nu");
                        int csIdx = findColIndex(colNames, "causo", "cau_so", "so_cau");
                        int lgIdx = findColIndex(colNames, "loigiai", "loi_giai", "ketqua", "noi_dung");

                        for (Object obj : allRows) {
                            Object[] row = (Object[]) obj;

                            // Check match mangId
                            boolean matchNH = (nhIdx < 0)
                                    || (row[nhIdx] != null && row[nhIdx].toString().equals(String.valueOf(mangId)));
                            // Check match thangSinh
                            boolean matchTS = (tsIdx < 0)
                                    || (row[tsIdx] != null && row[tsIdx].toString().equals(String.valueOf(thangSinh)));
                            // Check match gender
                            boolean matchGT = (gtIdx < 0)
                                    || (row[gtIdx] != null && (row[gtIdx].toString().equalsIgnoreCase(genderVn)
                                            || row[gtIdx].toString().toLowerCase().contains(genderVn.toLowerCase())));

                            if (matchNH && matchTS && matchGT) {
                                String cauSoVal = (csIdx >= 0 && row[csIdx] != null) ? row[csIdx].toString() : "N/A";
                                String loiGiaiVal = (lgIdx >= 0 && row[lgIdx] != null) ? row[lgIdx].toString() : "N/A";

                                content.append("<p><strong>Mạng ").append(mang)
                                        .append(", cầu số ").append(cauSoVal).append("</strong><br>")
                                        .append(loiGiaiVal).append("</p>");
                                foundContent = true;
                                break;
                            }
                        }
                    } catch (Exception eInner) {
                    }
                    
                }
            } catch (Exception e) {
                System.err.println("Lỗi manual Sở 7: " + e.getMessage());
            }
            
        
    }
}
