CREATE TABLE IF NOT EXISTS users (
    user_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    email     TEXT NOT NULL UNIQUE,
    age       INTEGER NOT NULL CHECK (age >= 0),
    role      TEXT NOT NULL CHECK (role IN ('ADMINISTRATOR', 'PARTICIPANT'))
);


CREATE TABLE IF NOT EXISTS user_credentials (
    user_id       INTEGER PRIMARY KEY,
    password_hash TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS conferences (
    conference_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    title           TEXT NOT NULL,
    capacity        INTEGER NOT NULL CHECK (capacity > 0),
    start_date_time TEXT NOT NULL,
    end_date_time   TEXT NOT NULL,
    location        TEXT,
    CHECK (end_date_time > start_date_time)
);

CREATE TABLE IF NOT EXISTS registrations (
    registration_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id           INTEGER NOT NULL,
    conference_id     INTEGER NOT NULL,
    status            TEXT NOT NULL CHECK (status IN ('CONFIRMED', 'WAITLISTED', 'CANCELLED')),
    waitlist_position INTEGER,
    registration_date TEXT NOT NULL,
    UNIQUE (user_id, conference_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (conference_id) REFERENCES conferences(conference_id) ON DELETE CASCADE
);
