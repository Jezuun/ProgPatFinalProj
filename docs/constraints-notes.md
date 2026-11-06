# Database Constraint Notes

These constraints protect the conference data from invalid or duplicate records. They are defined in `src/main/resources/db/schema.sql` unless noted otherwise.

## Users and credentials

- `users.email` is `NOT NULL` and `UNIQUE`, so every account has an email and no two users share one. A basic `CHECK` rejects values that do not resemble an email address.
- `first_name` and `last_name` must not be blank. `age` must be zero or greater, and `role` must be either `ADMIN` or `PARTICIPANT`.
- `credentials.user_id` is both its primary key and a foreign key to `users.id`. This enforces at most one credential row per user and keeps login data separate from profile data. Deleting a user cascades to that user's credentials.
- Password data is stored as a hash and salt in `credentials`; the profile table does not contain a password.

## Conferences

- `capacity` must be greater than zero.
- `title` cannot be blank, and both `start_time` and `end_time` are required. A `CHECK` requires `end_time` to be later than `start_time`.
- `UNIQUE (title, start_time)` prevents two conferences from having the same title at the same start time.
- Dates and times use the same ISO-8601 text format so the stored values can be compared consistently.

## Registrations

- `user_id` and `conference_id` are foreign keys to their parent records. Foreign-key enforcement is enabled on each connection by `Database.getConnection()`.
- `UNIQUE (user_id, conference_id)` prevents duplicate registration rows for the same participant and conference. When someone registers again after cancelling, `RegistrationDao` reuses the cancelled row.
- `status` is limited to `CONFIRMED`, `WAITLISTED`, or `CANCELLED`.
- A `CHECK` requires wait-listed rows to have a positive `waitlist_position`; confirmed and cancelled rows must have no position.
- A partial unique index on `(conference_id, waitlist_position)` for wait-listed rows ensures that two people cannot occupy the same queue position in one conference.

## Rules enforced by the DAO

Some rules depend on other rows and are handled in DAO transactions rather than by a single table constraint. `RegistrationDao` checks capacity and scheduling conflicts when registering, and processes FIFO promotion when a confirmed registration is cancelled. `ConferenceDao` prevents reducing capacity below the confirmed registration count and prevents deletion while confirmed or wait-listed registrations remain.
