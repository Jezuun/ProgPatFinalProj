package com.conference.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static String dbUrl = "jdbc:sqlite:conference.db";

    public static void setDbUrl(String url) {
        dbUrl = url;
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }
}