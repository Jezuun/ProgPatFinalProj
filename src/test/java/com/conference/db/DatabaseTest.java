package com.conference.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseTest {

    @TempDir
    Path tempDir;

    private Database database;

    @BeforeEach
    void setUp() {
        database = Database.forFile(tempDir.resolve("test.db"));
        database.reset();
    }

    @Test
    void eachConnectionEnforcesForeignKeys() throws SQLException {
        try (Connection connection = database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA foreign_keys")) {
            rs.next();
            assertEquals(1, rs.getInt(1));
        }
        assertThrows(SQLException.class, () -> {
            try (Connection connection = database.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                        "INSERT INTO registrations (user_id, conference_id, status) VALUES (999, 1, 'CONFIRMED')");
            }
        });
    }

    @Test
    void seedAndResetRestoreExpectedRows() throws SQLException {
        assertEquals(6, count("users"));
        assertEquals(6, count("credentials"));
        assertEquals(4, count("conferences"));
        assertEquals(8, count("registrations"));

        try (Connection connection = database.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM conferences");
        }
        assertEquals(0, count("conferences"));
        database.reset();
        assertEquals(6, count("users"));
        assertEquals(6, count("credentials"));
        assertEquals(4, count("conferences"));
        assertEquals(8, count("registrations"));
    }

    private int count(String table) throws SQLException {
        try (Connection connection = database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
