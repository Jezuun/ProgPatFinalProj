package com.conference.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

public class TestDb {

    public static void create() {
        try {
            Files.deleteIfExists(Path.of("target", "test.db"));
            Database.setDbUrl("jdbc:sqlite:target/test.db");

            runSqlFile("src/main/resources/db/schema.sql");
            runSqlFile("src/main/resources/db/seed.sql");
        } catch (Exception e) {
            throw new RuntimeException("Could not build test database", e);
        }
    }

    private static void runSqlFile(String fileName) throws Exception {
        String text = Files.readString(Path.of(fileName));

        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : text.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        }
    }
}