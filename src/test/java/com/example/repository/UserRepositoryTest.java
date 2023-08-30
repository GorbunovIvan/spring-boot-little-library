package com.example.repository;

import com.example.TestDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.HSQLDB)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestDataProvider testDataProvider;

    @BeforeEach
    void setUp() {

        testDataProvider.removeIds();

        userRepository.deleteAll();
        userRepository.saveAll(testDataProvider.getUsers());
    }

    @Test
    void testFindByUsername() {

        var users = testDataProvider.getUsers();
        assertFalse(users.isEmpty());

        for (var user : users) {
            var userOpt = userRepository.findByUsername(user.getUsername());
            assertTrue(userOpt.isPresent());
            assertEquals(user, userOpt.get());
        }

        assertTrue(userRepository.findByUsername("").isEmpty());
        assertTrue(userRepository.findByUsername("---").isEmpty());
    }
}