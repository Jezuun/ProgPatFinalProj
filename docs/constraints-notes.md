Database Constraint Notes

Profile and credentials are separate. users holds the profile (name, email, age, role). user_credentials holds only the password hash and is linked by user_id.

Constraint	Where	Why
Primary keys (user_id, conference_id, registration_id)	every table	Each row has a unique id. user_credentials.user_id is also its primary key, so a user has at most one credentials row.

UNIQUE on users.email	users	Two accounts cannot use the same email.

CHECK (age >= 0)	users	No negative ages.

CHECK (role IN ('ADMINISTRATOR','PARTICIPANT'))	users	Only the two valid roles.

CHECK (capacity > 0)	conferences	A conference needs at least one seat.

CHECK (end_date_time > start_date_time)	conferences	A conference cannot end before it starts. Dates are ISO text (2026-12-01T09:00:00), so they compare correctly as text.

UNIQUE (user_id, conference_id)	registrations	A participant cannot register twice for the same conference.

CHECK (status IN ('CONFIRMED','WAITLISTED','CANCELLED'))	registrations	Only valid statuses.

Foreign keys with ON DELETE CASCADE	credentials, registrations	A registration cannot point to a missing user or conference. Deleting a user also deletes the credentials row. Foreign keys are switched on in Database.getConnection().

Rules left for Deliverable 2 (services)

Capacity checks, scheduling conflicts, waiting-list promotion on cancellation, and "do not delete a conference that has registrations" will be added in the service layer.