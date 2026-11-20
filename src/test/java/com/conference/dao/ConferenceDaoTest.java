package com.conference.dao;

import com.conference.db.TestDb;
import com.conference.exception.DataAccessException;
import com.conference.model.Conference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConferenceDaoTest {
    private ConferenceDao conferenceDao;

    @BeforeEach
    void setUp() {
        TestDb.create();
        conferenceDao = new ConferenceDao();
    }

    @Test
    void testCreateAndFindAll() {
        conferenceDao.create(new Conference(0, "JavaFX Summit", 100, "2027-01-01T09:00:00", "2027-01-01T17:00:00", "Room A"));

        assertEquals(4, conferenceDao.findAll().size());
    }

    @Test
    void testFindById() {
        Conference conf = conferenceDao.findById(1);

        assertEquals("Food", conf.getTitle());
        assertNull(conferenceDao.findById(999));
    }

    @Test
    void testUpdate() {
        Conference conf = conferenceDao.findById(1);
        conf.setCapacity(200);
        conferenceDao.update(conf);

        assertEquals(200, conferenceDao.findById(1).getCapacity());
    }

    @Test
    void testDelete() {
        conferenceDao.delete(3);

        assertNull(conferenceDao.findById(3));
    }

    @Test
    void testInvalidCapacityAndDatesAreRejected() {
        Conference zeroCapacity = new Conference(0, "A", 0, "2027-01-01T09:00:00", "2027-01-01T10:00:00", null);
        Conference endBeforeStart = new Conference(0, "B", 10, "2027-01-01T10:00:00", "2027-01-01T09:00:00", null);

        assertThrows(DataAccessException.class, () -> conferenceDao.create(zeroCapacity));
        assertThrows(DataAccessException.class, () -> conferenceDao.create(endBeforeStart));
    }
}