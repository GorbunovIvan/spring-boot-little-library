package com.example.service;

import com.example.TestDataProvider;
import com.example.model.Book;
import com.example.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
class BookServiceTest {

    @Autowired
    private BookService bookService;
    
    @MockBean
    private BookRepository bookRepository;

    @Autowired
    private TestDataProvider testDataProvider;

    private Book newBook;

    @BeforeEach
    void setUp() {

        newBook = new Book(null, "1 new test book", 1111, Set.of(testDataProvider.getAuthors().get(0)), new HashSet<>());

        Mockito.reset(bookRepository);

        // books
        when(bookRepository.findAll()).thenReturn(testDataProvider.getBooks());
        when(bookRepository.findAllByNameIgnoreCase("")).thenReturn(Collections.emptyList());
        when(bookRepository.findById(-1)).thenReturn(Optional.empty());
        when(bookRepository.existsById(-1)).thenReturn(false);
        when(bookRepository.merge(newBook)).thenReturn(newBook);
        doNothing().when(bookRepository).deleteById(anyInt());

        for (var book : testDataProvider.getBooks()) {
            when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
            when(bookRepository.findAllByNameIgnoreCase(book.getName())).thenReturn(List.of(book));
            when(bookRepository.existsById(book.getId())).thenReturn(true);
            when(bookRepository.merge(book)).thenReturn(book);
        }

        // authors
        when(bookRepository.findAuthorByName("")).thenReturn(Optional.empty());
        for (var author : testDataProvider.getAuthors()) {
            when(bookRepository.findAuthorByName(author.getName())).thenReturn(Optional.of(author));
        }
    }

    @Test
    void testGetById() {

        var books = testDataProvider.getBooks();
        assertFalse(books.isEmpty());

        for (var book : books) {
            assertEquals(book, bookService.getById(book.getId()));
            verify(bookRepository, times(1)).findById(book.getId());
        }

        assertNull(bookService.getById(-1));
    }

    @Test
    void testGetAll() {

        var books = testDataProvider.getBooks();
        assertFalse(books.isEmpty());

        assertEquals(new HashSet<>(books), bookService.getAll());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void testGetAllByName() {

        var books = testDataProvider.getBooks();
        assertFalse(books.isEmpty());

        for (var book : books) {
            assertTrue(bookService.getAllByName(book.getName()).contains(book));
            verify(bookRepository, times(1)).findAllByNameIgnoreCase(book.getName());
        }

        assertTrue(bookService.getAllByName("").isEmpty());
    }

    @Test
    void testCreate() {
        assertEquals(newBook, bookService.create(newBook));
        verify(bookRepository, times(1)).merge(newBook);
    }

    @Test
    void testUpdate() {

        var books = testDataProvider.getBooks();
        assertFalse(books.isEmpty());

        for (var book : books) {
            assertEquals(book, bookService.update(book.getId(), book));
            verify(bookRepository, times(1)).existsById(book.getId());
            verify(bookRepository, times(1)).merge(book);
        }

        assertThrows(RuntimeException.class, () -> bookService.update(-1, newBook));
        verify(bookRepository, times(1)).existsById(-1);
        verify(bookRepository, never()).merge(newBook);
    }

    @Test
    void testDeleteById() {

        var books = testDataProvider.getBooks();
        assertFalse(books.isEmpty());

        for (var book : books) {
            bookService.deleteById(book.getId());
            verify(bookRepository, times(1)).deleteById(book.getId());
        }
    }

    @Test
    void testGetAuthorByName() {

        var authors = testDataProvider.getAuthors();
        assertFalse(authors.isEmpty());

        for (var author : authors) {
            assertEquals(author, bookService.getAuthorByName(author.getName()));
            verify(bookRepository, times(1)).findAuthorByName(author.getName());
        }

        assertNull(bookService.getAuthorByName(""));
    }
}