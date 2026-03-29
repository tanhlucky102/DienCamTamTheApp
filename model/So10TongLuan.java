package com.example.diencamtamthe.model;

import jakarta.persistence.*;

@Entity
@Table(name = "so10_tong_luan")
public class So10TongLuan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "thang_tho_thai")
    private Integer thangThoThai;

    @Column(name = "thang_sinh")
    private Integer thangSinh;

    @Column(name = "loi_doan", columnDefinition = "TEXT")
    private String loiDoan;

    public So10TongLuan() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getThangThoThai() { return thangThoThai; }
    public void setThangThoThai(Integer thangThoThai) { this.thangThoThai = thangThoThai; }

    public Integer getThangSinh() { return thangSinh; }
    public void setThangSinh(Integer thangSinh) { this.thangSinh = thangSinh; }

    public String getLoiDoan() { return loiDoan; }
    public void setLoiDoan(String loiDoan) { this.loiDoan = loiDoan; }
}
