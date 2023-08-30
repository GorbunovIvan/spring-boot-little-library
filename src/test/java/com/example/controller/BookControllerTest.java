package com.example.controller;

import com.example.TestDataProvider;
import com.example.model.Book;
import com.example.security.SecurityUtils;
import com.example.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private SecurityUtils securityUtils;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TestDataProvider testDataProvider;
    
    private Book newBook;
    
    @BeforeEach
    void setUp() {

        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        newBook = new Book(null, "1 new test book", 1111, Set.of(testDataProvider.getAuthors().get(0)), new HashSet<>());

        Mockito.reset(bookService, securityUtils);

        // bookService
        when(bookService.getAll()).thenReturn(new HashSet<>(testDataProvider.getBooks()));
        when(bookService.getById(-1)).thenReturn(null);
        when(bookService.create(newBook)).thenReturn(newBook);
        when(bookService.update(-1, newBook)).thenThrow(RuntimeException.class);
        when(bookService.update(null, newBook)).thenThrow(RuntimeException.class);
        doNothing().when(bookService).deleteById(anyInt());

        for (var book : testDataProvider.getBooks()) {
            when(bookService.getById(book.getId())).thenReturn(book);
            when(bookService.update(book.getId(), book)).thenReturn(book);
        }
    }

    @Test
    void testGetAll() throws Exception {

        var books = new HashSet<>(testDataProvider.getBooks());
        assertFalse(books.isEmpty());

        String result = mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/books"))
                .andExpect(model().attribute("books", books))
                .andReturn()
                .getResponse()
                .getContentAsString();

        for (var book : books) {
            assertTrue(result.contains(book.getName()));
        }

        verify(bookService, times(1)).getAll();
    }

    @Test
    void testGetById() throws Exception {

        var books = testDataProvider.getBooks();
        assertFalse(books.isEmpty());

        for (var book : books) {

            mvc.perform(get("/books/{id}", book.getId()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("books/book"))
                    .andExpect(model().attribute("book", book))
                    .andExpect(content().string(containsString(book.getName())))
                    .andExpect(content().string(containsString(String.valueOf(book.getYear()))))
                    .andExpect(content().string(containsString(book.getAuthorsAsString())));

            verify(bookService, times(1)).getById(book.getId());
        }

        mvc.perform(get("/books/{id}", -1))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verify(bookService, times(1)).getById(-1);
    }

    @Test
    void testCreateForm() throws Exception {

        mvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/new"))
                .andExpect(model().attribute("book", new Book()))
                .andExpect(content().string(containsString("Adding new book")))
                .andExpect(content().string(containsString("Add book")));
    }

    @Test
    void testCreate() throws Exception {

        // errors
        mvc.perform(post("/books")
                        .param("name", "")
                        .param("year", "")
                        .param("author0", "")
                        .param("author1", "")
                        .param("author2", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("books/new"));

        verify(bookService, never()).create(any(Book.class));

        // is ok
        mvc.perform(post("/books")
                        .param("name", newBook.getName())
                        .param("year", String.valueOf(newBook.getYear()))
                        .param("author0", newBook.getAuthors().iterator().next().getName()))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/books"));

        verify(bookService, times(1)).create(any(Book.class));
    }

    @Test
    void testUpdateForm() throws Exception {

        var books = testDataProvider.getBooks();
        assertFalse(books.isEmpty());

        for (var book : books) {

            mvc.perform(get("/books/{id}/edit", book.getId()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("books/edit"))
                    .andExpect(model().attribute("book", book))
                    .andExpect(content().string(containsString("Editing book")))
                    .andExpect(content().string(containsString("Update book")));

            verify(bookService, times(1)).getById(book.getId());
        }

        mvc.perform(get("/books/{id}/edit", -1))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verify(bookService, times(1)).getById(-1);
    }

    @Test
    void testUpdate() throws Exception {

        var books = testDataProvider.getBooks();
        assertFalse(books.isEmpty());

        // errors
        mvc.perform(post("/books/{id}", -1)
                        .param("name", newBook.getName())
                        .param("year", String.valueOf(newBook.getYear()))
                        .param("author0", getAuthorNameOfBook(newBook, 0))
                        .param("author1", getAuthorNameOfBook(newBook, 1))
                        .param("author2", getAuthorNameOfBook(newBook, 2)))
                .andExpect(status().isOk())
                .andExpect(view().name("error")); // id is not found

        for (var book : books) {

            // errors
            mvc.perform(post("/books/{id}", book.getId())
                            .param("name", "")
                            .param("year", "")
                            .param("author1", "")
                            .param("author2", "")
                            .param("author3", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("books/edit"));

            // is ok
            mvc.perform(post("/books/{id}", book.getId())
                            .param("name", book.getName())
                            .param("year", String.valueOf(book.getYear()))
                            .param("author0", getAuthorNameOfBook(book, 0))
                            .param("author1", getAuthorNameOfBook(book, 1))
                            .param("author2", getAuthorNameOfBook(book, 2)))
                    .andExpect(status().isFound())
                    .andExpect(view().name("redirect:/books/" + book.getId()));
        }

        verify(bookService, times(books.size() + 1)).update(anyInt(), any(Book.class));
    }

    @Test
    void testDelete() throws Exception {

        var books = testDataProvider.getBooks();
        assertFalse(books.isEmpty());

        for (var book : books) {

            mvc.perform(delete("/books/{id}", book.getId()))
                    .andExpect(status().isFound())
                    .andExpect(view().name("redirect:/books"));

            verify(bookService, times(1)).deleteById(book.getId());
        }
    }

    private String getAuthorNameOfBook(Book book, int index) {
        var authors = new ArrayList<>(book.getAuthors());
        if (authors.size() <= index) {
            return "";
        }
        return authors.get(index).getName();
    }
}