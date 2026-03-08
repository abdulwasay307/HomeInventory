package com.desktopapp.backend.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final DatabaseConfig instance = new DatabaseConfig();
    private static final String DB_HOST = "localhost";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Mysql@308";
    private static final String DB_NAME = "homeinventory";
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + "/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private DatabaseConfig() {
        initialize();
    }

    public static DatabaseConfig getInstance() {
        return instance;
    }

    private void initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Database driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                System.out.println("Database connection closed");
                connection.close(); 
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}