package com.example.DienCamTamThe.util;
import java.sql.*;

public class DBVerify {
    public static void main(String[] args) {
        String url = "jdbc:mysql://dien-cam-tam-the-nguyentrinhvo01012005-d6b5.j.aivencloud.com:17507/dien_cam_tam_the?sslMode=REQUIRED";
        String user = "avnadmin";
        String pass = ""; // REMOVED FOR SECURITY
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.err.println("CONNECTED TO MYSQL!");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            while (rs.next()) {
                String table = rs.getString(1);
                System.err.println("TABLE: " + table);
                try (Statement stmt2 = conn.createStatement();
                     ResultSet rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    if (rs2.next()) System.err.println("  Count: " + rs2.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
