package com.conference.dao;

import com.conference.db.TestDb;
import com.conference.exception.DataAccessException;
import com.conference.model.Role;
import com.conference.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoTest {
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        TestDb.create();
        userDao = new UserDao();
    }

    @Test
    void testCreateAndFindAll() {
        userDao.create(new User(0, "Kitty Cat", "kitty@test.com", 25, Role.PARTICIPANT), "hash");

        assertEquals(6, userDao.findAll().size());
    }

    @Test
    void testFindById() {
        User user = userDao.findById(2);

        assertEquals("Lebron James", user.getFullName());
        assertNull(userDao.findById(999));
    }

    @Test
    void testUpdate() {
        User user = userDao.findById(2);
        user.setFullName("Updated Name");
        userDao.update(user);

        assertEquals("Updated Name", userDao.findById(2).getFullName());
    }

    @Test
    void testDelete() {
        userDao.delete(5);

        assertNull(userDao.findById(5));
    }

    @Test
    void testDuplicateEmailIsRejected() {
        User duplicate = new User(0, "Other", "lebron@example.com", 30, Role.PARTICIPANT);

        assertThrows(DataAccessException.class, () -> userDao.create(duplicate, "hash"));
    }
}