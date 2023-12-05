package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String dbURL = "jdbc:mysql://localhost:3306/576Project"; // 数据库URL
    private static final String username = "root"; // 数据库用户名
    private static final String password = ""; // 数据库密码

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbURL, username, password);
    }
    public static void testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                System.out.println("数据库连接成功！");
                conn.close(); // 关闭连接
            }
        } catch (SQLException e) {
            System.out.println("数据库连接失败：");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testConnection(); // 调用测试函数
    }
}
