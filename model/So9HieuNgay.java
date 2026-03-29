package com.example.diencamtamthe.model;

import jakarta.persistence.*;

@Entity
@Table(name = "so9_hieu_ngay")
public class So9HieuNgay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_hieu", length = 50)
    private String tenHieu;

    @Column(name = "ngay_am_lich", length = 100)
    private String ngayAmLich;

    public So9HieuNgay() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTenHieu() { return tenHieu; }
    public void setTenHieu(String tenHieu) { this.tenHieu = tenHieu; }

    public String getNgayAmLich() { return ngayAmLich; }
    public void setNgayAmLich(String ngayAmLich) { this.ngayAmLich = ngayAmLich; }
}
