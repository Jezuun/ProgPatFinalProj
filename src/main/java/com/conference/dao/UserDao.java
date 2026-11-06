package com.conference.dao;

import com.conference.db.Database;
import com.conference.exception.DataAccessException;
import com.conference.model.Credentials;
import com.conference.model.Role;
import com.conference.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Database access for users (profile table) and their credentials. */
public class UserDao extends BaseDao {

    private static final String SELECT_USER =
            "SELECT id, first_name, last_name, email, age, role FROM users";

    public UserDao(Database database) {
        super(database);
    }

    /**
     * Saves a profile and its credentials together.
     */
    public int createWithCredentials(User user, String passwordHash, String salt) {
        String insertUser =
                "INSERT INTO users (first_name, last_name, email, age, role) VALUES (?, ?, ?, ?, ?)";
        String insertCredentials =
                "INSERT INTO credentials (user_id, password_hash, salt) VALUES (?, ?, ?)";
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps =
                             connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, user.getFirstName());
                    ps.setString(2, user.getLastName());
                    ps.setString(3, user.getEmail());
                    ps.setInt(4, user.getAge());
                    ps.setString(5, user.getRole().name());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        id = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = connection.prepareStatement(insertCredentials)) {
                    ps.setInt(1, id);
                    ps.setString(2, passwordHash);
                    ps.setString(3, salt);
                    ps.executeUpdate();
                }
                connection.commit();
                user.setId(id);
                return id;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw fail("Cannot create user " + user.getEmail(), e);
        }
    }

    public Optional<User> findById(int id) {
        return findOne(SELECT_USER + " WHERE id = ?", id);
    }

    public Optional<User> findByEmail(String email) {
        return findOne(SELECT_USER + " WHERE email = ?", email);
    }

    /** Returns password data for the account with this email address. */
    public Optional<Credentials> findCredentialsByEmail(String email) {
        String sql = "SELECT c.user_id, c.password_hash, c.salt FROM credentials c "
                + "JOIN users u ON u.id = c.user_id WHERE u.email = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Credentials(rs.getInt("user_id"),
                            rs.getString("password_hash"), rs.getString("salt")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw fail("Cannot read credentials for " + email, e);
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = SELECT_USER + " ORDER BY last_name, first_name";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(map(rs));
            }
            return users;
        } catch (SQLException e) {
            throw fail("Cannot list users", e);
        }
    }

    /**
     * Updates a user's profile (not the password).
     *
     * @param user the user with new values; its id selects the row
     * @return true if a row was updated
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, age = ?, role = ? WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setInt(4, user.getAge());
            ps.setString(5, user.getRole().name());
            ps.setInt(6, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw fail("Cannot update user " + user.getId(), e);
        }
    }

    /**
     * Changes a user's password hash and salt.
     *
     * @param userId       user id
     * @param passwordHash new hash
     * @param salt         new salt
     * @return true if a row was updated
     */
    public boolean updatePassword(int userId, String passwordHash, String salt) {
        String sql = "UPDATE credentials SET password_hash = ?, salt = ? WHERE user_id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setString(2, salt);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw fail("Cannot update password for user " + userId, e);
        }
    }

    /**
     * Deletes a user. Credentials and registrations are removed by ON DELETE CASCADE.
     *
     * @param id user id
     * @return true if a row was deleted
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw fail("Cannot delete user " + id, e);
        }
    }

    private Optional<User> findOne(String sql, Object parameter) {
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, parameter);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw fail("Cannot read user", e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),
                rs.getString("email"), rs.getInt("age"), Role.valueOf(rs.getString("role")));
    }
}
