package com.conference.dao;

import com.conference.db.Database;
import com.conference.exception.DataAccessException;
import com.conference.model.Credentials;
import com.conference.model.Role;
import com.conference.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDaoTest {

    @TempDir
    Path tempDir;

    private UserDao dao;

    @BeforeEach
    void setUp() {
        Database database = Database.forFile(tempDir.resolve("test.db"));
        database.reset();
        dao = new UserDao(database);
    }

    @Test
    void seedAdminCanBeFoundByEmail() {
        Optional<User> admin = dao.findByEmail("admin@conference.local");
        assertTrue(admin.isPresent());
        assertEquals(Role.ADMIN, admin.get().getRole());
        assertEquals(40, admin.get().getAge());
    }

    @Test
    void createWithCredentialsSavesProfileAndPasswordData() {
        User user = new User("Zoe", "Martin", "zoe@example.com", 20, Role.PARTICIPANT);
        int id = dao.createWithCredentials(user, "hash", "salt");

        assertTrue(id > 0);
        assertEquals(id, user.getId());
        assertEquals(20, dao.findById(id).orElseThrow().getAge());
        Credentials credentials = dao.findCredentialsByEmail("zoe@example.com").orElseThrow();
        assertEquals(id, credentials.userId());
        assertEquals("hash", credentials.passwordHash());
    }

    @Test
    void failedCredentialsInsertRollsBackTheProfile() {
        User user = new User("Zoe", "Martin", "zoe@example.com", 20, Role.PARTICIPANT);

        assertThrows(DataAccessException.class,
                () -> dao.createWithCredentials(user, null, "salt"));

        assertFalse(dao.findByEmail("zoe@example.com").isPresent());
    }

    @Test
    void duplicateEmailAndNegativeAgeAreRejected() {
        User duplicate = new User("Alice", "Copy", "alice@example.com", 22, Role.PARTICIPANT);
        assertThrows(DataAccessException.class,
                () -> dao.createWithCredentials(duplicate, "hash", "salt"));

        User invalidAge = new User("Pat", "Example", "pat@example.com", -1, Role.PARTICIPANT);
        assertThrows(DataAccessException.class,
                () -> dao.createWithCredentials(invalidAge, "hash", "salt"));
    }

    @Test
    void updateChangesProfileFields() {
        User alice = dao.findByEmail("alice@example.com").orElseThrow();
        alice.setLastName("Tremblay-Roy");
        alice.setEmail("alice.new@example.com");
        alice.setAge(23);

        assertTrue(dao.update(alice));
        User reloaded = dao.findById(alice.getId()).orElseThrow();
        assertEquals("Tremblay-Roy", reloaded.getLastName());
        assertEquals("alice.new@example.com", reloaded.getEmail());
        assertEquals(23, reloaded.getAge());
    }
}
