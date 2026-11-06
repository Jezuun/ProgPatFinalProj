-- Sample accounts and conference data. Run after schema.sql.
-- Passwords are stored as SHA-256 hex of (salt + password).
--   admin@conference.local / admin123 (administrator)
--   participant emails   password: password1
-- Hash formula:
--   hex(sha256(salt + password))
PRAGMA foreign_keys = ON;

INSERT INTO users (id, first_name, last_name, email, age, role) VALUES
    (1, 'Admin',  'System',  'admin@conference.local',  40, 'ADMIN'),
    (2, 'Alice',  'Tremblay','alice@example.com',       22, 'PARTICIPANT'),
    (3, 'Bob',    'Gagnon',  'bob@example.com',         24, 'PARTICIPANT'),
    (4, 'Chloe',  'Roy',     'chloe@example.com',       21, 'PARTICIPANT'),
    (5, 'David',  'Lavoie',  'david@example.com',       25, 'PARTICIPANT'),
    (6, 'Emma',   'Bouchard','emma@example.com',        23, 'PARTICIPANT');

INSERT INTO credentials (user_id, password_hash, salt) VALUES
    (1, '36346fd60ab6cbf51edbcf47d62b1da712c21dbda5dbc0d1eabe8e53a230d72c', 'cms-salt-1'),
    (2, '629979993ac1fb4d9bfe3993277caf7c6e92da633231f5d239d1edf87793d01e', 'cms-salt-1'),
    (3, '629979993ac1fb4d9bfe3993277caf7c6e92da633231f5d239d1edf87793d01e', 'cms-salt-1'),
    (4, '629979993ac1fb4d9bfe3993277caf7c6e92da633231f5d239d1edf87793d01e', 'cms-salt-1'),
    (5, '629979993ac1fb4d9bfe3993277caf7c6e92da633231f5d239d1edf87793d01e', 'cms-salt-1'),
    (6, '629979993ac1fb4d9bfe3993277caf7c6e92da633231f5d239d1edf87793d01e', 'cms-salt-1');

-- 3 and 4 overlap on purpose (scheduling-conflict testing)
-- 2 has capacity 2 (waiting-list testing)
INSERT INTO conferences (id, title, description, location, start_time, end_time, capacity) VALUES
    (1, 'Java Developers Summit',  'Modern Java, JavaFX and tooling.',      'Montreal Convention Centre', '2026-12-10T09:00', '2026-12-10T17:00', 100),
    (2, 'Database Design Workshop','Hands-on SQL and schema design.',       'Room B-204',                 '2026-12-11T09:00', '2026-12-11T12:00', 2),
    (3, 'Cloud Computing Forum',   'Cloud architecture and deployment.',    'Auditorium A',               '2026-12-12T13:00', '2026-12-12T16:00', 50),
    (4, 'Cybersecurity Essentials','Practical security for developers.',    'Room C-110',                 '2026-12-12T14:00', '2026-12-12T17:00', 30);

-- Conference 2 is full (Alice, Bob confirmed); Chloe, David waitlisted FIFO.
INSERT INTO registrations (user_id, conference_id, status, waitlist_position, registered_at) VALUES
    (2, 1, 'CONFIRMED',  NULL, '2026-11-01T10:00:00'),
    (3, 1, 'CONFIRMED',  NULL, '2026-11-01T10:05:00'),
    (2, 2, 'CONFIRMED',  NULL, '2026-11-01T11:00:00'),
    (3, 2, 'CONFIRMED',  NULL, '2026-11-01T11:05:00'),
    (4, 2, 'WAITLISTED', 1,    '2026-11-01T11:10:00'),
    (5, 2, 'WAITLISTED', 2,    '2026-11-01T11:15:00'),
    (6, 3, 'CONFIRMED',  NULL, '2026-11-02T09:00:00'),
    (4, 4, 'CANCELLED',  NULL, '2026-11-02T09:30:00');


