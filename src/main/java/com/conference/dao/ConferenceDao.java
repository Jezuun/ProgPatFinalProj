package com.conference.dao;

import com.conference.db.Database;
import com.conference.exception.DataAccessException;
import com.conference.model.Conference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Database access for conferences. */
public class ConferenceDao extends BaseDao {

    private static final String SELECT =
            "SELECT id, title, description, location, start_time, end_time, capacity FROM conferences";

    public ConferenceDao(Database database) {
        super(database);
    }

    public int create(Conference conference) {
        String sql = "INSERT INTO conferences (title, description, location, start_time, end_time, capacity) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, conference);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                conference.setId(keys.getInt(1));
                return conference.getId();
            }
        } catch (SQLException e) {
            throw fail("Cannot create conference " + conference.getTitle(), e);
        }
    }

    public Optional<Conference> findById(int id) {
        String sql = SELECT + " WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw fail("Cannot read conference " + id, e);
        }
    }

    public List<Conference> findAll() {
        List<Conference> conferences = new ArrayList<>();
        String sql = SELECT + " ORDER BY start_time, title";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                conferences.add(map(rs));
            }
            return conferences;
        } catch (SQLException e) {
            throw fail("Cannot list conferences", e);
        }
    }

    public boolean update(Conference conference) {
        String sql = "UPDATE conferences SET title = ?, description = ?, location = ?, "
                + "start_time = ?, end_time = ?, capacity = ? WHERE id = ?";
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int confirmed = countConfirmed(connection, conference.getId());
                if (conference.getCapacity() < confirmed) {
                    throw new DataAccessException("Capacity cannot be lower than confirmed registrations");
                }
                if (hasSchedulingConflict(connection, conference)) {
                    throw new DataAccessException("Conference times conflict with a participant's other registration");
                }
                boolean updated;
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    bind(ps, conference);
                    ps.setInt(7, conference.getId());
                    updated = ps.executeUpdate() > 0;
                }
                connection.commit();
                return updated;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw fail("Cannot update conference " + conference.getId(), e);
        }
    }

    /** Deletes a conference only when it has no confirmed or waitlisted participants. */
    public boolean delete(int id) {
        String sql = "DELETE FROM conferences WHERE id = ?";
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String active = "SELECT COUNT(*) FROM registrations WHERE conference_id = ? "
                        + "AND status IN ('CONFIRMED', 'WAITLISTED')";
                try (PreparedStatement ps = connection.prepareStatement(active)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            throw new DataAccessException("Cannot delete a conference with active registrations");
                        }
                    }
                }
                boolean deleted;
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    deleted = ps.executeUpdate() > 0;
                }
                connection.commit();
                return deleted;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw fail("Cannot delete conference " + id, e);
        }
    }

    private int countConfirmed(Connection connection, int conferenceId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM registrations WHERE conference_id = ? AND status = 'CONFIRMED'")) {
            ps.setInt(1, conferenceId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private boolean hasSchedulingConflict(Connection connection, Conference conference) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations current_reg "
                + "JOIN registrations other ON other.user_id = current_reg.user_id "
                + "JOIN conferences c ON c.id = other.conference_id "
                + "WHERE current_reg.conference_id = ? AND current_reg.status = 'CONFIRMED' "
                + "AND other.status = 'CONFIRMED' AND other.conference_id <> ? "
                + "AND ? < c.end_time AND ? > c.start_time";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, conference.getId());
            ps.setInt(2, conference.getId());
            ps.setString(3, conference.getStartTime().format(Database.MINUTE_FORMAT));
            ps.setString(4, conference.getEndTime().format(Database.MINUTE_FORMAT));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private void bind(PreparedStatement ps, Conference c) throws SQLException {
        ps.setString(1, c.getTitle());
        ps.setString(2, c.getDescription());
        ps.setString(3, c.getLocation());
        ps.setString(4, c.getStartTime().format(Database.MINUTE_FORMAT));
        ps.setString(5, c.getEndTime().format(Database.MINUTE_FORMAT));
        ps.setInt(6, c.getCapacity());
    }

    private Conference map(ResultSet rs) throws SQLException {
        return new Conference(rs.getInt("id"), rs.getString("title"), rs.getString("description"),
                rs.getString("location"), LocalDateTime.parse(rs.getString("start_time")),
                LocalDateTime.parse(rs.getString("end_time")), rs.getInt("capacity"));
    }
}


