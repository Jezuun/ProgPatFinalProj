package com.conference.dao;

import com.conference.db.TestDb;
import com.conference.exception.DataAccessException;
import com.conference.model.Registration;
import com.conference.model.RegistrationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;




import static org.junit.jupiter.api.Assertions.*;

class RegistrationDaoTest {
    private RegistrationDao registrationDao;

    @BeforeEach
    void setUp() {
        TestDb.create();
        registrationDao = new RegistrationDao();
    }

    @Test
    void testCreateAndFindAll() {
        registrationDao.create(new Registration(0, 5, 1, RegistrationStatus.CONFIRMED, null, "2026-11-06T10:00:00"));

        assertEquals(5, registrationDao.findAll().size());
    }

    @Test
    void testFindById() {
        Registration reg = registrationDao.findById(4);

        assertEquals(RegistrationStatus.WAITLISTED, reg.getStatus());
        assertEquals(1, reg.getWaitlistPosition());
        assertNull(registrationDao.findById(999));
    }

    @Test
    void testUpdateStatus() {
        registrationDao.updateStatus(4, RegistrationStatus.CONFIRMED, null);

        Registration reg = registrationDao.findById(4);
        assertEquals(RegistrationStatus.CONFIRMED, reg.getStatus());
        assertNull(reg.getWaitlistPosition());
    }

    @Test
    void testDelete() {
        registrationDao.delete(4);

        assertNull(registrationDao.findById(4));
    }

    @Test
    void testDuplicateRegistrationIsRejected() {
        Registration duplicate = new Registration(0, 2, 1, RegistrationStatus.CONFIRMED, null, "2026-11-06T10:00:00");

        assertThrows(DataAccessException.class, () -> registrationDao.create(duplicate));
    }
}