package com.conference.dao;

import  com.conference.db.Database;
import com.conference.exception.DataAccessException;
import com.conference.model.Conference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConferenceDao {
    private static final Logger LOGGER = Logger.getLogger(ConferenceDao.class.getName());

    public void create(Conference conf) {
        String sql = "INSERT INTO conferences (title, capacity, start_date_time, end_date_time, location) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, conf.getTitle());
            stmt.setInt(2, conf.getCapacity());
            stmt.setString(3, conf.getStartDateTime());
            stmt.setString(4, conf.getEndDateTime());
            stmt.setString(5, conf.getLocation());
            stmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "couldnt create conference", e);
            throw new DataAccessException("failed to create conference", e);
        }
    }

    public Conference findById(int conferenceId) {
        String sql = "SELECT * FROM conferences WHERE conference_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conferenceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapConference(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "could not find conference ID: " + conferenceId, e);
            throw new DataAccessException("unable to find conference", e);
        }
        return null;
    }

    public List<Conference> findAll() {
        List<Conference> list = new ArrayList<>();
        String sql = "SELECT * FROM conferences";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapConference(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "error getting all conferences", e);
            throw new DataAccessException("failed to get conferences", e);
        }
        return list;
    }

    public void update(Conference conf) {
        String sql = "UPDATE conferences SET title = ?, capacity = ?, start_date_time = ?, end_date_time = ?, location = ? WHERE conference_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, conf.getTitle());
            stmt.setInt(2, conf.getCapacity());
            stmt.setString(3, conf.getStartDateTime());
            stmt.setString(4, conf.getEndDateTime());
            stmt.setString(5, conf.getLocation());
            stmt.setInt(6, conf.getConferenceId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "couldnt update conferencee: " + conf.getConferenceId(), e);
            throw new DataAccessException("failed to update conference", e);
        }
    }

    public void delete(int conferenceId) {
        String sql = "DELETE FROM conferences WHERE conference_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conferenceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "error deleting conference: " + conferenceId, e);
            throw new DataAccessException("failed to delete conference", e);
        }
    }

    private Conference mapConference(ResultSet rs) throws SQLException {
        return new Conference(
            rs.getInt("conference_id"),
            rs.getString("title"),
            rs.getInt("capacity"),
            rs.getString("start_date_time"),
            rs.getString("end_date_time"),
            rs.getString("location")
        );
    }
}
