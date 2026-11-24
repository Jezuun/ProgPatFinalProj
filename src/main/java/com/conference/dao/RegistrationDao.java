package com.conference.dao;

import com.conference.db.Database;
import com.conference.exception.DataAccessException;
import com.conference.model.Conference;
import com.conference.model.Registration;
import com.conference.model.RegistrationStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegistrationDao {
    private static final Logger LOGGER = Logger.getLogger(RegistrationDao.class.getName());

    public Registration findById(int registrationId) {
        String sql = "SELECT * FROM registrations WHERE registration_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, registrationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRegistration(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "could not read registration: " + registrationId, e);
            throw new DataAccessException("Failed to find registration", e);
        }
        return null;
    }

    public List<Registration> findAll() {
        List<Registration> list = new ArrayList<>();
        String sql = "SELECT * FROM registrations";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRegistration(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "unsuccessful to get registration", e);
            throw new DataAccessException("failed to get registration", e);
        }
        return list;
    }

    public static void create(Registration reg) {
        String sql = "INSERT INTO registrations (user_id, conference_id, status, waitlist_position, registration_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reg.getUserId());
            stmt.setInt(2, reg.getConferenceId());
            stmt.setString(3, reg.getStatus().name());
            if (reg.getWaitlistPosition() != null) {
                stmt.setInt(4, reg.getWaitlistPosition());
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }
            stmt.setString(5, reg.getRegistrationDate());
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "error making registration", e);
            throw new DataAccessException("failed to create registration", e);
        }
    }

    public static void updateStatus(int userId, int conferenceId, RegistrationStatus status, Integer waitlistPosition) {
        String sql = "UPDATE registrations SET status = ?, waitlist_position = ? WHERE user_id = ? AND conference_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            if (waitlistPosition != null) {
                stmt.setInt(2, waitlistPosition);
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setInt(3, userId);
            stmt.setInt(4, conferenceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "cant update registration status: " + userId, e);
            throw new DataAccessException("failed to update registration status", e);
        }
    }

    public void delete(int registrationId) {
        String sql = "DELETE FROM registrations WHERE registration_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, registrationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "unsuccessful deleting registration: " + registrationId, e);
            throw new DataAccessException("failed to delete registration", e);
        }
    }

    private Registration mapRegistration(ResultSet rs) throws SQLException {
        Integer waitlistPos = rs.getObject("waitlist_position") != null ? rs.getInt("waitlist_position") : null;
        return new Registration(
            rs.getInt("registration_id"),
            rs.getInt("user_id"),
            rs.getInt("conference_id"),
            RegistrationStatus.valueOf(rs.getString("status")),
            waitlistPos,
            rs.getString("registration_date")
        );
    }

    public static int findRegistCountById(int conferenceId) {
        String sql = "SELECT COUNT(*) FROM registrations WHERE conference_id = ? AND status = 'CONFIRMED'";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conferenceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Could not count registrations for conference: " + conferenceId, e);
            throw new DataAccessException("Failed to count registrations", e);
        }
        return 0;
    }

    public static List<Conference> findRegisteredConferences(int userId) {
        List<Conference> list = new ArrayList<>();
        String sql = "SELECT c.* FROM conferences c INNER JOIN registrations r ON c.conference_id = r.conference_id WHERE r.user_id = ? AND (r.status = 'CONFIRMED' OR r.status = 'WAITLISTED')";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Conference(
                            rs.getInt("conference_id"),
                            rs.getString("title"),
                            rs.getInt("capacity"),
                            rs.getString("start_date_time"),
                            rs.getString("end_date_time"),
                            rs.getString("location")
                    ));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not find registered conferences for user: " + userId, e);
            throw new DataAccessException("Failed to find registered conferences", e);
        }

        return list;
    }

    public static List<Conference> findWaitListConferences(int userId) {
        List<Conference> list = new ArrayList<>();
        String sql = "SELECT c.* FROM conferences c INNER JOIN registrations r ON c.conference_id = r.conference_id WHERE r.user_id = ? AND r.status = 'WAITLISTED'";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Conference(
                            rs.getInt("conference_id"),
                            rs.getString("title"),
                            rs.getInt("capacity"),
                            rs.getString("start_date_time"),
                            rs.getString("end_date_time"),
                            rs.getString("location")
                    ));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not find registered conferences for user: " + userId, e);
            throw new DataAccessException("Failed to find registered conferences", e);
        }

        return list;
    }

    public static int findWLPos(int conferenceId, int userId) {
        String sql = "SELECT waitlist_position FROM registrations WHERE conference_id = ? AND user_id = ? AND status = 'WAITLISTED'";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conferenceId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Could not find waitlist position for conference: " + conferenceId, e);
            throw new DataAccessException("Failed to find waitlist position", e);
        }
        return 0;
    }

    public static int findHighestWLPos(int conferenceId) {
        String sql = "SELECT COALESCE(MAX(waitlist_position), 0) FROM registrations WHERE conference_id = ? AND status = 'WAITLISTED'";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conferenceId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Could not find highest waitlist position for conference: " + conferenceId, e);
            throw new DataAccessException("Failed to find highest waitlist position", e);
        }
        return 0;
    }

    public static String findStatus(int conferenceId, int userId) {
        String sql = "SELECT status FROM registrations WHERE conference_id = ? AND user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conferenceId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getString(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Could not find registration status for conference: " + conferenceId, e);
            throw new DataAccessException("Failed to find registration status", e);
        }
        return null;
    }

    public static void decrementWLPos(int conferenceId, int newPosition) {
        String sql = "UPDATE registrations SET waitlist_position = waitlist_position - 1 WHERE conference_id = ? AND status = 'WAITLISTED' AND waitlist_position > ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conferenceId);
            stmt.setInt(2, newPosition);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Could not update waitlist positions", e);
            throw new DataAccessException(
                    "Failed to decrease waitlist positions", e);
        }
    }
}
