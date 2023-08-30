package com.example.controller.converter;

import com.example.TestDataProvider;
import com.example.model.Author;
import com.example.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuthorConverterTest {

    @Autowired
    private AuthorConverter authorConverter;

    @MockBean
    private BookService bookService;

    @Autowired
    private TestDataProvider testDataProvider;

    @BeforeEach
    void setUp() {
        when(bookService.getAuthorByName("")).thenReturn(null);
        for (var author : testDataProvider.getAuthors()) {
            when(bookService.getAuthorByName(author.getName())).thenReturn(author);
        }
    }

    @Test
    void testConvert() {

        var authors = testDataProvider.getAuthors();
        assertFalse(authors.isEmpty());

        for (var author : authors) {
            var authorFound = authorConverter.convert(author.getName());
            assertNotNull(authorFound);
            assertEquals(author, authorFound);
            assertNotNull(authorFound.getId()); // checking if the author exists (was not just created)
            verify(bookService, times(1)).getAuthorByName(author.getName());
        }

        assertNull(authorConverter.convert(""));
        verify(bookService, never()).getAuthorByName("");

        // 'authorConverter.convert()' must create new if it does not find
        assertEquals(new Author("---"), authorConverter.convert("---"));
        verify(bookService, times(1)).getAuthorByName("---");
    }
}