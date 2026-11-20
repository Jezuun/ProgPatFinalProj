package com.conference.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {

    @BeforeEach
    void setUp() {
        TestDb.create();
    }

    @Test
    void foreignKeysAreOnForEveryConnection() throws SQLException {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA foreign_keys")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void seedDataIsLoaded() throws SQLException {
        assertEquals(5, count("users"));
        assertEquals(5, count("user_credentials"));
        assertEquals(3, count("conferences"));
        assertEquals(4, count("registrations"));
    }

    @Test
    void registrationWithUnknownUserIsRejected() {
        assertThrows(SQLException.class, () -> {
            try (Connection conn = Database.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("INSERT INTO registrations (user_id, conference_id, status, registration_date) "
                        + "VALUES (999, 1, 'CONFIRMED', '2026-11-01T10:00:00')");
            }
        });
    }

    private int count(String table) throws SQLException {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
