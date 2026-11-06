package com.conference.dao;

import com.conference.db.Database;
import com.conference.exception.DataAccessException;
import com.conference.model.Registration;
import com.conference.model.RegistrationStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Database access for registrations, including the multi-step operations
 * that register a participant and cancel a registration with FIFO promotion.
 */
public class RegistrationDao extends BaseDao {

    private static final String SELECT =
            "SELECT id, user_id, conference_id, status, waitlist_position, registered_at FROM registrations";

    public RegistrationDao(Database database) {
        super(database);
    }

    /** Inserts an explicit row; use registerParticipant for the normal signup flow. */
    public int create(Registration registration) {
        String sql = "INSERT INTO registrations (user_id, conference_id, status, waitlist_position, registered_at) "
                + "VALUES (?, ?, ?, ?, ?)";
        LocalDateTime when = registration.getRegisteredAt() != null
                ? registration.getRegisteredAt() : now();
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (registration.getStatus() == RegistrationStatus.CONFIRMED
                        || registration.getStatus() == RegistrationStatus.WAITLISTED) {
                    int capacity = readCapacity(connection, registration.getConferenceId());
                    int confirmed = countByStatus(connection, registration.getConferenceId(),
                            RegistrationStatus.CONFIRMED);
                    if (registration.getStatus() == RegistrationStatus.CONFIRMED && confirmed >= capacity) {
                        throw new DataAccessException("Conference capacity has been reached");
                    }
                    if (hasSchedulingConflict(connection, registration.getUserId(), registration.getConferenceId())) {
                        throw new DataAccessException("Registration conflicts with another confirmed conference");
                    }
                }
                try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, registration.getUserId());
                    ps.setInt(2, registration.getConferenceId());
                    ps.setString(3, registration.getStatus().name());
                    setPosition(ps, 4, registration.getWaitlistPosition());
                    ps.setString(5, when.format(Database.SECOND_FORMAT));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        registration.setId(keys.getInt(1));
                        registration.setRegisteredAt(when);
                    }
                }
                connection.commit();
                return registration.getId();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw fail("Cannot create registration", e);
        }
    }

    public Optional<Registration> findById(int id) {
        try (Connection connection = database.getConnection()) {
            return findById(connection, id);
        } catch (SQLException e) {
            throw fail("Cannot read registration " + id, e);
        }
    }

    public Optional<Registration> findByUserAndConference(int userId, int conferenceId) {
        try (Connection connection = database.getConnection()) {
            return findByUserAndConference(connection, userId, conferenceId);
        } catch (SQLException e) {
            throw fail("Cannot read registration", e);
        }
    }

    public List<Registration> findByConference(int conferenceId) {
        return findMany(SELECT + " WHERE conference_id = ? ORDER BY registered_at, id", conferenceId);
    }

    public List<Registration> findByUser(int userId) {
        return findMany(SELECT + " WHERE user_id = ? ORDER BY registered_at, id", userId);
    }

    /**
     * Returns the waiting list of a conference in FIFO order (position 1 first).
     *
     * @param conferenceId conference id
     * @return waitlisted registrations ordered by position
     */
    public List<Registration> findWaitlist(int conferenceId) {
        return findMany(SELECT + " WHERE conference_id = ? AND status = 'WAITLISTED' "
                + "ORDER BY waitlist_position", conferenceId);
    }

    public int countByStatus(int conferenceId, RegistrationStatus status) {
        try (Connection connection = database.getConnection()) {
            return countByStatus(connection, conferenceId, status);
        } catch (SQLException e) {
            throw fail("Cannot count registrations for conference " + conferenceId, e);
        }
    }

    public boolean update(Registration registration) {
        try (Connection connection = database.getConnection()) {
            return update(connection, registration.getId(), registration.getStatus(),
                    registration.getWaitlistPosition());
        } catch (SQLException e) {
            throw fail("Cannot update registration " + registration.getId(), e);
        }
    }

    /**
     * Permanently deletes a registration row. To cancel a registration
     * (and promote the next person), use {@link #cancelRegistration(int)}.
     *
     * @param id registration id
     * @return true if a row was deleted
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM registrations WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw fail("Cannot delete registration " + id, e);
        }
    }

    /**
     * Registers a participant. In one transaction it reads the conference
     * capacity, counts confirmed seats, and saves the registration as
     * CONFIRMED (seat available) or WAITLISTED at the end of the queue (full).
     * A previously cancelled registration is reused.
     *
     * @param userId       participant id
     * @param conferenceId conference id
     * @return the saved registration
     * @throws DataAccessException if the conference does not exist, the participant is
     *                             already registered, or on SQL error
     */
    public Registration registerParticipant(int userId, int conferenceId) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int capacity = readCapacity(connection, conferenceId);
                Optional<Registration> existing = findByUserAndConference(connection, userId, conferenceId);
                if (existing.isPresent() && existing.get().getStatus() != RegistrationStatus.CANCELLED) {
                    throw new DataAccessException("Participant " + userId
                            + " is already registered for conference " + conferenceId);
                }
                if (hasSchedulingConflict(connection, userId, conferenceId)) {
                    throw new DataAccessException("Registration conflicts with another confirmed conference");
                }

                boolean seatFree = countByStatus(connection, conferenceId, RegistrationStatus.CONFIRMED) < capacity;
                RegistrationStatus status = seatFree ? RegistrationStatus.CONFIRMED : RegistrationStatus.WAITLISTED;
                Integer position = seatFree ? null : lastWaitlistPosition(connection, conferenceId) + 1;
                LocalDateTime when = now();

                Registration result;
                if (existing.isPresent()) {
                    result = existing.get();
                    updateRow(connection, result.getId(), status, position, when);
                } else {
                    result = new Registration(0, userId, conferenceId, status, position, when);
                    result.setId(insertRow(connection, result));
                }
                result.setStatus(status);
                result.setWaitlistPosition(position);
                result.setRegisteredAt(when);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw fail("Cannot register participant " + userId + " for conference " + conferenceId, e);
        }
    }

    /**
     * Cancels a registration in one transaction. If a confirmed seat is freed,
     * the first person in the waiting list (FIFO) is promoted to CONFIRMED.
     * The remaining waiting-list positions are then renumbered 1, 2, 3...
     *
     * @param registrationId id of the registration to cancel
     * @return the promoted registration, or empty if nobody was promoted
     * @throws DataAccessException if the registration does not exist, is already
     *                             cancelled, or on SQL error
     */
    public Optional<Registration> cancelRegistration(int registrationId) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Registration registration = findById(connection, registrationId).orElseThrow(
                        () -> new DataAccessException("Registration " + registrationId + " not found"));
                if (registration.getStatus() == RegistrationStatus.CANCELLED) {
                    throw new DataAccessException("Registration " + registrationId + " is already cancelled");
                }
                boolean freedSeat = registration.getStatus() == RegistrationStatus.CONFIRMED;
                int conferenceId = registration.getConferenceId();

                update(connection, registrationId, RegistrationStatus.CANCELLED, null);

                Optional<Registration> promoted = Optional.empty();
                if (freedSeat) {
                    List<Registration> queue = findWaitlist(connection, conferenceId);
                    for (Registration candidate : queue) {
                        if (!hasSchedulingConflict(connection, candidate.getUserId(), conferenceId)) {
                            update(connection, candidate.getId(), RegistrationStatus.CONFIRMED, null);
                            candidate.setStatus(RegistrationStatus.CONFIRMED);
                            candidate.setWaitlistPosition(null);
                            promoted = Optional.of(candidate);
                            break;
                        }
                    }
                }
                renumberWaitlist(connection, conferenceId);
                connection.commit();
                return promoted;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw fail("Cannot cancel registration " + registrationId, e);
        }
    }

    private Optional<Registration> findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT + " WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    private Optional<Registration> findByUserAndConference(Connection connection, int userId,
                                                           int conferenceId) throws SQLException {
        try (PreparedStatement ps =
                     connection.prepareStatement(SELECT + " WHERE user_id = ? AND conference_id = ?")) {
            ps.setInt(1, userId);
            ps.setInt(2, conferenceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    private List<Registration> findWaitlist(Connection connection, int conferenceId) throws SQLException {
        String sql = SELECT + " WHERE conference_id = ? AND status = 'WAITLISTED' ORDER BY waitlist_position";
        List<Registration> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, conferenceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    private int readCapacity(Connection connection, int conferenceId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT capacity FROM conferences WHERE id = ?")) {
            ps.setInt(1, conferenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DataAccessException("Conference " + conferenceId + " not found");
                }
                return rs.getInt(1);
            }
        }
    }

    private boolean hasSchedulingConflict(Connection connection, int userId, int conferenceId)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations r "
                + "JOIN conferences target ON target.id = ? "
                + "JOIN conferences existing ON existing.id = r.conference_id "
                + "WHERE r.user_id = ? AND r.status = 'CONFIRMED' AND r.conference_id <> ? "
                + "AND target.start_time < existing.end_time "
                + "AND target.end_time > existing.start_time";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, conferenceId);
            ps.setInt(2, userId);
            ps.setInt(3, conferenceId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private int countByStatus(Connection connection, int conferenceId, RegistrationStatus status)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations WHERE conference_id = ? AND status = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, conferenceId);
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int lastWaitlistPosition(Connection connection, int conferenceId) throws SQLException {
        String sql = "SELECT COALESCE(MAX(waitlist_position), 0) FROM registrations "
                + "WHERE conference_id = ? AND status = 'WAITLISTED'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, conferenceId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int insertRow(Connection connection, Registration r) throws SQLException {
        String sql = "INSERT INTO registrations (user_id, conference_id, status, waitlist_position, registered_at) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getUserId());
            ps.setInt(2, r.getConferenceId());
            ps.setString(3, r.getStatus().name());
            setPosition(ps, 4, r.getWaitlistPosition());
            ps.setString(5, r.getRegisteredAt().format(Database.SECOND_FORMAT));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private void updateRow(Connection connection, int id, RegistrationStatus status, Integer position,
                           LocalDateTime when) throws SQLException {
        String sql = "UPDATE registrations SET status = ?, waitlist_position = ?, registered_at = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            setPosition(ps, 2, position);
            ps.setString(3, when.format(Database.SECOND_FORMAT));
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    private boolean update(Connection connection, int id, RegistrationStatus status, Integer position)
            throws SQLException {
        String sql = "UPDATE registrations SET status = ?, waitlist_position = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            setPosition(ps, 2, position);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void renumberWaitlist(Connection connection, int conferenceId) throws SQLException {
        // Read the queue first, then update one row at a time in order.
        // Going in ascending order always moves a person into a spot that is already free,
        // so the unique (conference_id, waitlist_position) index is never violated.
        List<Registration> queue = findWaitlist(connection, conferenceId);
        String sql = "UPDATE registrations SET waitlist_position = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < queue.size(); i++) {
                ps.setInt(1, i + 1);
                ps.setInt(2, queue.get(i).getId());
                ps.executeUpdate();
            }
        }
    }

    private List<Registration> findMany(String sql, int parameter) {
        List<Registration> list = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, parameter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw fail("Cannot list registrations", e);
        }
    }

    private void setPosition(PreparedStatement ps, int index, Integer position) throws SQLException {
        if (position == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setInt(index, position);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    private Registration map(ResultSet rs) throws SQLException {
        int position = rs.getInt("waitlist_position");
        Integer waitlistPosition = rs.wasNull() ? null : position;
        return new Registration(rs.getInt("id"), rs.getInt("user_id"), rs.getInt("conference_id"),
                RegistrationStatus.valueOf(rs.getString("status")), waitlistPosition,
                LocalDateTime.parse(rs.getString("registered_at")));
    }
}

