-- SQLite schema for the conference management system.
-- Database.getConnection() enables foreign keys on every connection.
-- Dates/times are stored as ISO-8601 text (yyyy-MM-dd'T'HH:mm).

DROP TABLE IF EXISTS registrations;
DROP TABLE IF EXISTS credentials;
DROP TABLE IF EXISTS conferences;
DROP TABLE IF EXISTS users;
-- users: profile data only (no password here)
CREATE TABLE users (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name  TEXT NOT NULL CHECK (length(trim(first_name)) > 0),
    last_name   TEXT NOT NULL CHECK (length(trim(last_name)) > 0),
    email       TEXT NOT NULL UNIQUE CHECK (email LIKE '%_@_%._%'),
    age         INTEGER NOT NULL CHECK (age >= 0),
    role        TEXT NOT NULL DEFAULT 'PARTICIPANT'
                     CHECK (role IN ('ADMIN', 'PARTICIPANT')),
    created_at  TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M', 'now'))
);
-- credentials: login data, 1-to-1 with users, kept separate
CREATE TABLE credentials (
    user_id        INTEGER PRIMARY KEY,
    password_hash  TEXT NOT NULL,
    salt           TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
-- conferences
CREATE TABLE conferences (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    title        TEXT NOT NULL CHECK (length(trim(title)) > 0),
    description  TEXT,
    location     TEXT,
    start_time   TEXT NOT NULL,
    end_time     TEXT NOT NULL,
    capacity     INTEGER NOT NULL CHECK (capacity > 0),
    CHECK (end_time > start_time),
    UNIQUE (title, start_time)
);
-- registrations: links a participant to a conference
-- status + waitlist position drive the FIFO waiting list
CREATE TABLE registrations (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id            INTEGER NOT NULL,
    conference_id      INTEGER NOT NULL,
    status             TEXT NOT NULL
                            CHECK (status IN ('CONFIRMED', 'WAITLISTED', 'CANCELLED')),
    waitlist_position  INTEGER,
    registered_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%S', 'now')),
    FOREIGN KEY (user_id)       REFERENCES users(id)       ON DELETE CASCADE,
    FOREIGN KEY (conference_id) REFERENCES conferences(id) ON DELETE CASCADE,
    -- no duplicate registration for the same person and conference
    UNIQUE (user_id, conference_id),
    -- position exists only (and always) when WAITLISTED
    CHECK (
        (status = 'WAITLISTED' AND waitlist_position IS NOT NULL AND waitlist_position > 0)
        OR
        (status <> 'WAITLISTED' AND waitlist_position IS NULL)
    )
);

-- two people can't hold the same waiting-list spot in one conference
CREATE UNIQUE INDEX ux_registrations_waitlist
    ON registrations (conference_id, waitlist_position)
    WHERE status = 'WAITLISTED';

-- speed up the common lookups
CREATE INDEX ix_registrations_conference ON registrations (conference_id, status);
CREATE INDEX ix_registrations_user       ON registrations (user_id);



