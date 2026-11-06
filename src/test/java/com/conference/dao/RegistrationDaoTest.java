package com.conference.dao;

import com.conference.db.Database;
import com.conference.exception.DataAccessException;
import com.conference.model.Registration;
import com.conference.model.RegistrationStatus;
import com.conference.model.Conference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrationDaoTest {

    private static final int ALICE = 2;
    private static final int BOB = 3;
    private static final int DAVID = 5;
    private static final int EMMA = 6;

    @TempDir
    Path tempDir;

    private RegistrationDao dao;
    private ConferenceDao conferences;

    @BeforeEach
    void setUp() {
        Database database = Database.forFile(tempDir.resolve("test.db"));
        database.reset();
        dao = new RegistrationDao(database);
        conferences = new ConferenceDao(database);
    }

    @Test
    void registrationUsesAvailableSeatOrWaitlist() {
        Registration confirmed = dao.registerParticipant(EMMA, 1);
        assertEquals(RegistrationStatus.CONFIRMED, confirmed.getStatus());
        assertNull(confirmed.getWaitlistPosition());

        Registration waiting = dao.registerParticipant(EMMA, 2);
        assertEquals(RegistrationStatus.WAITLISTED, waiting.getStatus());
        assertEquals(3, waiting.getWaitlistPosition());
    }

    @Test
    void duplicateRegistrationIsRejected() {
        assertThrows(DataAccessException.class, () -> dao.registerParticipant(ALICE, 1));
    }

    @Test
    void confirmedScheduleConflictBlocksRegistration() {
        dao.create(new Registration(0, EMMA, 4, RegistrationStatus.CANCELLED, null, null));
        assertThrows(DataAccessException.class, () -> dao.registerParticipant(EMMA, 4));
        Conference full = conferences.findById(4).orElseThrow();
        full.setCapacity(1);
        conferences.update(full);
        dao.create(new Registration(0, BOB, 4, RegistrationStatus.CONFIRMED, null, null));
        assertThrows(DataAccessException.class, () -> dao.registerParticipant(EMMA, 4));
        assertEquals(0, dao.findWaitlist(4).size());
        assertEquals(RegistrationStatus.CANCELLED,
                dao.findByUserAndConference(EMMA, 4).orElseThrow().getStatus());
    }

    @Test
    void sessionsThatTouchAreNotConflicts() {
        Conference adjacent = new Conference("Evening Session", "", "Room 2",
                LocalDateTime.of(2026, 12, 10, 17, 0),
                LocalDateTime.of(2026, 12, 10, 18, 0), 20);
        int id = conferences.create(adjacent);

        Registration registration = dao.registerParticipant(ALICE, id);
        assertEquals(RegistrationStatus.CONFIRMED, registration.getStatus());
    }

    @Test
    void cancellationSkipsConflictedWaitlistCandidate() {
        dao.create(new Registration(0, BOB, 4, RegistrationStatus.CONFIRMED, null, null));
        Registration bob = dao.findByUserAndConference(BOB, 4).orElseThrow();
        dao.create(new Registration(0, ALICE, 4, RegistrationStatus.WAITLISTED, 1, null));
        dao.create(new Registration(0, DAVID, 4, RegistrationStatus.WAITLISTED, 2, null));
        dao.create(new Registration(0, ALICE, 3, RegistrationStatus.CONFIRMED, null, null));

        Optional<Registration> promoted = dao.cancelRegistration(bob.getId());

        assertTrue(promoted.isPresent());
        assertEquals(DAVID, promoted.get().getUserId());
        List<Registration> waitlist = dao.findWaitlist(4);
        assertEquals(1, waitlist.size());
        assertEquals(ALICE, waitlist.get(0).getUserId());
        assertEquals(1, waitlist.get(0).getWaitlistPosition());
    }

    @Test
    void cancellingWaitlistedEntryRenumbersQueue() {
        Registration chloe = dao.findByUserAndConference(4, 2).orElseThrow();
        dao.cancelRegistration(chloe.getId());

        List<Registration> waitlist = dao.findWaitlist(2);
        assertEquals(1, waitlist.size());
        assertEquals(DAVID, waitlist.get(0).getUserId());
        assertEquals(1, waitlist.get(0).getWaitlistPosition());
        assertEquals(2, dao.countByStatus(2, RegistrationStatus.CONFIRMED));
    }

    @Test
    void cancelledRegistrationCanBeUsedAgain() {
        Registration cancelled = dao.findByUserAndConference(4, 4).orElseThrow();
        Registration reused = dao.registerParticipant(4, 4);

        assertEquals(cancelled.getId(), reused.getId());
        assertEquals(RegistrationStatus.CONFIRMED, reused.getStatus());
    }
}
