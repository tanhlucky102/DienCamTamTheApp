package com.example.diencamtamthe.model;

import jakarta.persistence.*;

@Entity
@Table(name = "so9_loi_doan")
public class So9LoiDoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_hieu", length = 50)
    private String tenHieu;

    @Column(name = "loi_doan", columnDefinition = "TEXT")
    private String loiDoan;

    public So9LoiDoan() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTenHieu() { return tenHieu; }
    public void setTenHieu(String tenHieu) { this.tenHieu = tenHieu; }

    public String getLoiDoan() { return loiDoan; }
    public void setLoiDoan(String loiDoan) { this.loiDoan = loiDoan; }
}
