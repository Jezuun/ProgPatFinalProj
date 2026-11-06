package com.conference.db;

import com.conference.exception.DataAccessException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The only class that opens database connections.
 *
 * <p>The database URL lives in {@link #DEFAULT_URL}. Every connection returned
 * by {@link #getConnection()} has foreign keys switched on, because SQLite
 * ignores them unless {@code PRAGMA foreign_keys = ON} runs on each connection.</p>
 */
public class Database {

    public static final String DEFAULT_URL = "jdbc:sqlite:conference.db";

    public static final DateTimeFormatter MINUTE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public static final DateTimeFormatter SECOND_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final Logger LOG = Logger.getLogger(Database.class.getName());
    private static final String SCHEMA_RESOURCE = "/db/schema.sql";
    private static final String SEED_RESOURCE = "/db/seed.sql";

    private final String url;

    public Database() {
        this(DEFAULT_URL);
    }

    public Database(String url) {
        this.url = url;
    }

    public static Database forFile(Path file) {
        return new Database("jdbc:sqlite:" + file.toAbsolutePath());
    }

    /**
     * Opens a new connection with foreign keys enabled.
     * The caller must close it (use try-with-resources).
     *
     * @return an open connection
     * @throws DataAccessException if the connection cannot be opened
     */
    public Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(url);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            } catch (SQLException e) {
                connection.close();
                throw e;
            }
            return connection;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot open database connection: " + url, e);
            throw new DataAccessException("Cannot open database connection", e);
        }
    }

    /**
     * Drops and recreates all tables, then inserts the sample data.
     * This is also how the database is reset.
     */
    public void reset() {
        runScript(SCHEMA_RESOURCE);
        runScript(SEED_RESOURCE);
    }

    public void runScript(String resource) {
        String script = readResource(resource);
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : script.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql.trim());
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Script failed: " + resource, e);
            throw new DataAccessException("Cannot run script " + resource, e);
        }
    }

    private String readResource(String resource) {
        try (InputStream in = Database.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new DataAccessException("Script not found: " + resource);
            }
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            // drop full-line comments so splitting on ';' is safe
            return reader.lines()
                    .filter(line -> !line.trim().startsWith("--"))
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot read script: " + resource, e);
            throw new DataAccessException("Cannot read script " + resource, e);
        }
    }
}
