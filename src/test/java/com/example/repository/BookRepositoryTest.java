package com.example.repository;

import com.example.TestDataProvider;
import com.example.model.Author;
import com.example.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.HSQLDB)
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestDataProvider testDataProvider;

    @BeforeEach
    void setUp() {

        testDataProvider.removeIds();
        testDataProvider.getBooks().forEach(b -> b.getBorrowingRecords().clear());

        bookRepository.deleteAll();
        bookRepository.saveAll(testDataProvider.getBooks());
    }

    @Test
    void testFindAllByNameIgnoreCase() {

        var books = testDataProvider.getBooks();
        assertFalse(books.isEmpty());

        for (var book : books) {

            // lower case
            var nameLower = book.getName().toLowerCase();
            var booksExpected = books.stream().filter(b -> b.getName().equalsIgnoreCase(nameLower)).toList();
            assertEquals(booksExpected, bookRepository.findAllByNameIgnoreCase(nameLower));

            // upper case
            var nameUpper = book.getName().toUpperCase();
            booksExpected = books.stream().filter(b -> b.getName().equalsIgnoreCase(nameUpper)).toList();
            assertEquals(booksExpected, bookRepository.findAllByNameIgnoreCase(nameUpper));
        }

        assertTrue(bookRepository.findAllByNameIgnoreCase("").isEmpty());
        assertTrue(bookRepository.findAllByNameIgnoreCase("---").isEmpty());
    }

    @Test
    void testFindAuthorByName() {

        var authors = testDataProvider.getAuthors();
        assertFalse(authors.isEmpty());

        for (var author : authors) {
            var authorOpt = bookRepository.findAuthorByName(author.getName());
            assertTrue(authorOpt.isPresent());
            assertEquals(author, authorOpt.get());
        }

        assertFalse(bookRepository.findAuthorByName("").isPresent());
        assertFalse(bookRepository.findAuthorByName("---").isPresent());
    }

    @Test
    void testMerge() {

        int countBooks = bookRepository.findAll().size();

        var authors = testDataProvider.getAuthors();

        var book = new Book(null, "1 new test book", 1111, Set.of(authors.get(0), authors.get(1)), new HashSet<>());
        assertEquals(bookRepository.merge(book), book);

        book = new Book(null, "2 new test book", 2222, Set.of(new Author("1 new test author")), new HashSet<>());
        assertEquals(bookRepository.merge(book), book);

        book = new Book(null, "3 new test book", 3333, Set.of(authors.get(0), new Author("2 new test author")), new HashSet<>());
        assertEquals(bookRepository.merge(book), book);

        book = testDataProvider.getBooks().get(0);
        assertEquals(bookRepository.merge(book), book);

        assertEquals(bookRepository.findAll().size(), countBooks + 3);
    }
}