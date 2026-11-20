-- Administrator: admin@conference.com / admin123
-- Participants:  password1

INSERT INTO users (user_id, full_name, email, age, role) VALUES
    (1, 'Admin User',   'admin@conference.com', 35, 'ADMINISTRATOR'),
    (2, 'Lebron James',     'lebron@example.com',     28, 'PARTICIPANT'),
    (3, 'Chen When',  'chen@example.com',    31, 'PARTICIPANT'),
    (4, 'Rikachy GM',   'rikachy@example.com',     24, 'PARTICIPANT'),
    (5, 'Micky Mouse','micky@example.com',   26, 'PARTICIPANT');

INSERT INTO user_credentials (user_id, password_hash) VALUES
    (1, '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9'),
    (2, '0b14d501a594442a01c6859541bcb3e8164d183d32937b851835442f69d5c94e'),
    (3, '0b14d501a594442a01c6859541bcb3e8164d183d32937b851835442f69d5c94e'),
    (4, '0b14d501a594442a01c6859541bcb3e8164d183d32937b851835442f69d5c94e'),
    (5, '0b14d501a594442a01c6859541bcb3e8164d183d32937b851835442f69d5c94e');

INSERT INTO conferences (conference_id, title, capacity, start_date_time, end_date_time, location) VALUES
    (1, 'Food',     100, '2026-12-01T09:00:00', '2026-12-01T17:00:00', 'D-210'),
    (2, 'Javaa',   2, '2026-12-10T10:00:00', '2026-12-10T15:00:00', 'D-243'),
    (3, 'C# study',     50, '2026-12-10T12:00:00', '2026-12-10T16:00:00', 'D-239');

INSERT INTO registrations (user_id, conference_id, status, waitlist_position, registration_date) VALUES
    (2, 1, 'CONFIRMED',  NULL, '2026-11-01T10:00:00'),
    (2, 2, 'CONFIRMED',  NULL, '2026-11-01T10:05:00'),
    (3, 2, 'CONFIRMED',  NULL, '2026-11-01T10:10:00'),
    (4, 2, 'WAITLISTED', 1,    '2026-11-01T10:15:00');
