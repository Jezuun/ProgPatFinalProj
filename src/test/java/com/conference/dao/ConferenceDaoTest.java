package com.conference.dao;

import com.conference.db.Database;
import com.conference.exception.DataAccessException;
import com.conference.model.Conference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConferenceDaoTest {

    @TempDir
    Path tempDir;

    private ConferenceDao dao;

    @BeforeEach
    void setUp() {
        Database database = Database.forFile(tempDir.resolve("test.db"));
        database.reset();
        dao = new ConferenceDao(database);
    }

    private Conference sample() {
        return new Conference("AI Day", "Talks about AI", "Room 1",
                LocalDateTime.of(2027, 1, 20, 9, 0), LocalDateTime.of(2027, 1, 20, 12, 0), 25);
    }

    @Test
    void findAllReturnsConferencesInStartTimeOrder() {
        List<Conference> all = dao.findAll();
        assertEquals(4, all.size());
        assertTrue(all.get(0).getStartTime().isBefore(all.get(1).getStartTime()));
    }

    @Test
    void createThenFindById() {
        Conference conference = sample();
        int id = dao.create(conference);

        Conference loaded = dao.findById(id).orElseThrow();
        assertEquals("AI Day", loaded.getTitle());
        assertEquals(LocalDateTime.of(2027, 1, 20, 9, 0), loaded.getStartTime());
        assertEquals(25, loaded.getCapacity());
    }

    @Test
    void updateChangesFields() {
        Conference conference = dao.findById(1).orElseThrow();
        conference.setTitle("Java Summit 2026");
        conference.setCapacity(150);

        assertTrue(dao.update(conference));
        Conference reloaded = dao.findById(1).orElseThrow();
        assertEquals("Java Summit 2026", reloaded.getTitle());
        assertEquals(150, reloaded.getCapacity());

        Conference overlapping = dao.findById(2).orElseThrow();
        overlapping.setStartTime(LocalDateTime.of(2026, 12, 10, 12, 0));
        overlapping.setEndTime(LocalDateTime.of(2026, 12, 10, 15, 0));
        assertThrows(DataAccessException.class, () -> dao.update(overlapping));
    }

    @Test
    void capacityCannotBeReducedBelowConfirmedCount() {
        Conference conference = dao.findById(2).orElseThrow();
        conference.setCapacity(1);
        assertThrows(DataAccessException.class, () -> dao.update(conference));
    }

    @Test
    void invalidCapacityAndEndTimeAreRejected() {
        Conference conference = sample();
        conference.setCapacity(0);
        assertThrows(DataAccessException.class, () -> dao.create(conference));

        conference.setCapacity(25);
        conference.setEndTime(conference.getStartTime().minusHours(1));
        assertThrows(DataAccessException.class, () -> dao.create(conference));
    }

    @Test
    void deleteRequiresNoActiveRegistrations() {
        assertThrows(DataAccessException.class, () -> dao.delete(1));
        assertTrue(dao.delete(4));
    }
}
