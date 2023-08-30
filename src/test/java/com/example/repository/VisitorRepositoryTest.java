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
class VisitorRepositoryTest {

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private TestDataProvider testDataProvider;

    @BeforeEach
    void setUp() {

        testDataProvider.removeIds();
        testDataProvider.getVisitors().forEach(b -> b.getBorrowingRecords().clear());

        visitorRepository.deleteAll();
        visitorRepository.saveAll(testDataProvider.getVisitors());
    }

    @Test
    void testFindByNameIgnoreCase() {

        var visitors = testDataProvider.getVisitors();
        assertFalse(visitors.isEmpty());

        for (var visitor : visitors) {

            // lower case
            var name = visitor.getName().toLowerCase();
            var visitorOpt = visitorRepository.findByNameIgnoreCase(name);
            assertTrue(visitorOpt.isPresent());
            assertEquals(visitor, visitorOpt.get());

            // upper case
            name = visitor.getName().toUpperCase();
            visitorOpt = visitorRepository.findByNameIgnoreCase(name);
            assertTrue(visitorOpt.isPresent());
            assertEquals(visitor, visitorOpt.get());
        }

        assertTrue(visitorRepository.findByNameIgnoreCase("").isEmpty());
        assertTrue(visitorRepository.findByNameIgnoreCase("---").isEmpty());
    }
}