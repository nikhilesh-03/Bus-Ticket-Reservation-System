package com.masai.utility;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/ORCLCDB";
    private static final String USER = "SYS AS SYSDBA";
    private static final String PASSWORD = "mypassword1";

    public static Connection getConnection() throws SQLException {
        System.out.println("Database connection successful !");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
