package com.example.diencamtamthe.model;

import jakarta.persistence.*;

@Entity
@Table(name = "so8_gio_sinh_tam_the")
public class So8GioSinhTamThe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "gio_sinh", length = 20)
    private String gioSinh;

    @Column(name = "phan_gio", length = 20)
    private String phanGio;

    @Column(name = "tinh_trang_cha_me", length = 50)
    private String tinhTrangChaMe;

    @Column(name = "loi_doan", columnDefinition = "TEXT")
    private String loiDoan;

    public So8GioSinhTamThe() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getGioSinh() { return gioSinh; }
    public void setGioSinh(String gioSinh) { this.gioSinh = gioSinh; }

    public String getPhanGio() { return phanGio; }
    public void setPhanGio(String phanGio) { this.phanGio = phanGio; }

    public String getTinhTrangChaMe() { return tinhTrangChaMe; }
    public void setTinhTrangChaMe(String tinhTrangChaMe) { this.tinhTrangChaMe = tinhTrangChaMe; }

    public String getLoiDoan() { return loiDoan; }
    public void setLoiDoan(String loiDoan) { this.loiDoan = loiDoan; }
}
