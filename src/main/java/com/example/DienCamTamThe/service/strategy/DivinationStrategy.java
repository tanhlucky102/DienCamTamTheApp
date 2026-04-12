package com.example.DienCamTamThe.service.strategy;

import com.example.DienCamTamThe.entity.DivinationRequest;

public interface DivinationStrategy {
    int getSectionNumber();
    String getSectionTitle();
    void process(StringBuilder content, DivinationRequest request, 
                 String can, String chi, int ngaySinh, int thangSinh,
                 String canChiGio, int thangThoThai, String mang, String cot,
                 int truongSanhId, String gioSinhFull);
}
