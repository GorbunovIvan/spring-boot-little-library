package com.example.controller.converter;

import com.example.TestDataProvider;
import com.example.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class BookConverterTest {

    @Autowired
    private BookConverter bookConverter;

    @MockBean
    private BookService bookService;

    @Autowired
    private TestDataProvider testDataProvider;

    @BeforeEach
    void setUp() {

        when(bookService.getAllByName("")).thenReturn(Collections.emptyList());
        when(bookService.getAllByName("---")).thenReturn(Collections.emptyList());

        for (var book : testDataProvider.getBooks()) {
            var name = book.getName();
            var booksByName = testDataProvider.getBooks().stream().filter(b -> b.getName().equalsIgnoreCase(name)).toList();
            when(bookService.getAllByName(name)).thenReturn(booksByName);
        }
    }

    @Test
    void testConvert() {

        var books = testDataProvider.getBooks();
        assertFalse(books.isEmpty());

        for (var book : books) {

            // by single name (title)
            var name = book.getName();
            var bookFound = bookConverter.convert(name);
            assertNotNull(bookFound);
            assertEquals(book.getName(), bookFound.getName());
            verify(bookService, times(1)).getAllByName(name);

            // by full name
            name = book.getFullName();
            bookFound = bookConverter.convert(name);
            assertEquals(book, bookFound);
            verify(bookService, times(1)).getAllByName(name);

            // by full name but only with each of the authors
            if (book.getAuthors().size() > 1) {
                var authors = new HashSet<>(book.getAuthors());
                for (var author : book.getAuthors()) {
                    book.setAuthors(Set.of(author));
                    name = book.getFullName();
                    book.setAuthors(authors);
                    bookFound = bookConverter.convert(name);
                    assertEquals(book, bookFound);
                    verify(bookService, times(1)).getAllByName(name);
                }
            }
        }

        assertNull(bookConverter.convert(""));
        verify(bookService, never()).getAllByName("");

        assertNull(bookConverter.convert("---"));
        verify(bookService, times(1)).getAllByName("---");
    }
}