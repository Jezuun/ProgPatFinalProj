package com.conference.dao;

import com.conference.db.Database;
import com.conference.exception.DataAccessException;
import com.conference.model.Role;
import com.conference.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDao {
    private static final Logger LOGGER = Logger.getLogger(UserDao.class.getName());

    public void create(User user, String passwordHash) {
        String userSql = "INSERT INTO users (full_name, email, age, role) VALUES (?, ?, ?, ?)";
        String credSql = "INSERT INTO user_credentials (user_id, password_hash) VALUES ((SELECT MAX(user_id) FROM users), ?)";
        try (Connection conn = Database.getConnection()) {
            try (PreparedStatement stmt1 = conn.prepareStatement(userSql)) {
                stmt1.setString(1, user.getFullName());
                stmt1.setString(2, user.getEmail());
                stmt1.setInt(3, user.getAge());
                stmt1.setString(4, user.getRole().name());
                stmt1.executeUpdate();
            }

            try (PreparedStatement stmt2 = conn.prepareStatement(credSql)) {
                stmt2.setString(1, passwordHash);
                stmt2.executeUpdate();
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "couldnt create user", e);
            throw new DataAccessException("unsuccessful to create user", e);
        }
    }

    public User findByEmail(String userEmail) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "error finding user by email: " + userEmail, e);
            throw new DataAccessException("could not find the user by email", e);
        }
    }

    public String getPasswordHash(int userId) {
        String sql = "SELECT password_hash FROM user_credentials WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("password_hash") : null;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "error reading credentials for user " + userId, e);
            throw new DataAccessException("could not read credentials", e);
        }
    }

    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }

            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "error with userid: " + userId, e);
            throw new DataAccessException("could not find the user", e);
        }
        return null;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "unsuccessful retrival", e);
            throw new DataAccessException("failed to get users", e);
        }
        return users;
    }

    public void update(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, age = ?, role = ? WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setInt(3, user.getAge());
            stmt.setString(4, user.getRole().name());
            stmt.setInt(5, user.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "couldnt update user: " + user.getUserId(), e);
            throw new DataAccessException("update for user failed", e);
        }
    }

    public void delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "couldnt delete user: " + userId, e);
            throw new DataAccessException("user deletion failed", e);
        }
    }

    private static User mapUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getInt("age"),
            Role.valueOf(rs.getString("role"))
        );
    }
}
