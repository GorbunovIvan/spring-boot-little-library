package com.example.controller;

import com.example.TestDataProvider;
import com.example.model.Book;
import com.example.model.BorrowingRecord;
import com.example.model.Visitor;
import com.example.security.SecurityUtils;
import com.example.service.BookService;
import com.example.service.BorrowingRecordService;
import com.example.service.UserService;
import com.example.service.VisitorService;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BorrowingRecordControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BorrowingRecordService borrowingRecordService;
    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private BookService bookService; // for converter
    @MockBean
    private VisitorService visitorService; // for converter
    @MockBean
    private UserService userService; // for converter

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TestDataProvider testDataProvider;
    
    private BorrowingRecord newBorrowingRecord;
    
    @BeforeEach
    void setUp() {

        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        var freeBook = testDataProvider.getBooks().stream().filter(Book::isFree).findAny().orElse(null);
        newBorrowingRecord = new BorrowingRecord(null, freeBook, testDataProvider.getVisitors().get(0), testDataProvider.getUsers().get(0), LocalDateTime.now(), null);

        Mockito.reset(borrowingRecordService, securityUtils, bookService, visitorService, userService);

        // borrowingRecordsService
        when(borrowingRecordService.getAll()).thenReturn(new HashSet<>(testDataProvider.getBorrowingRecords()));
        when(borrowingRecordService.getById(-1L)).thenReturn(null);
        when(borrowingRecordService.create(newBorrowingRecord)).thenReturn(newBorrowingRecord);
        when(borrowingRecordService.update(-1L, newBorrowingRecord)).thenThrow(RuntimeException.class);
        when(borrowingRecordService.update(null, newBorrowingRecord)).thenThrow(RuntimeException.class);
        doNothing().when(borrowingRecordService).deleteById(anyLong());

        for (var borrowingRecord : testDataProvider.getBorrowingRecords()) {
            when(borrowingRecordService.getById(borrowingRecord.getId())).thenReturn(borrowingRecord);
            when(borrowingRecordService.update(borrowingRecord.getId(), borrowingRecord)).thenReturn(borrowingRecord);
        }

        // bookService
        when(bookService.getAllByName("")).thenReturn(Collections.emptyList());
        for (var book : testDataProvider.getBooks()) {
            when(bookService.getAllByName(book.getName())).thenReturn(List.of(book));
        }

        // visitorService
        when(visitorService.getByName("")).thenReturn(null);
        when(visitorService.create(any(Visitor.class))).thenReturn(new Visitor());
        for (var visitor : testDataProvider.getVisitors()) {
            when(visitorService.getByName(visitor.getName())).thenReturn(visitor);
        }

        // userService
        when(userService.getByUsername("")).thenReturn(null);
        for (var user : testDataProvider.getUsers()) {
            when(userService.getByUsername(user.getUsername())).thenReturn(user);
        }
    }

    @Test
    void testGetAll() throws Exception {

        var borrowingRecords = new HashSet<>(testDataProvider.getBorrowingRecords());
        assertFalse(borrowingRecords.isEmpty());

        String result = mvc.perform(get("/borrowing-records"))
                .andExpect(status().isOk())
                .andExpect(view().name("borrowingRecords/borrowingRecords"))
                .andExpect(model().attribute("borrowingRecords", borrowingRecords))
                .andReturn()
                .getResponse()
                .getContentAsString();

        for (var borrowingRecord : borrowingRecords) {
            assertTrue(result.contains(borrowingRecord.getBook().getName()));
        }

        verify(borrowingRecordService, times(1)).getAll();
    }

    @Test
    void testGetById() throws Exception {

        var borrowingRecords = testDataProvider.getBorrowingRecords();
        assertFalse(borrowingRecords.isEmpty());

        for (var borrowingRecord : borrowingRecords) {

            mvc.perform(get("/borrowing-records/{id}", borrowingRecord.getId()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("borrowingRecords/borrowingRecord"))
                    .andExpect(model().attribute("borrowingRecord", borrowingRecord))
                    .andExpect(content().string(containsString(borrowingRecord.getBook().getName())));

            verify(borrowingRecordService, times(1)).getById(borrowingRecord.getId());
        }

        mvc.perform(get("/borrowing-records/{id}", -1L))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verify(borrowingRecordService, times(1)).getById(-1L);
    }

    @Test
    void testCreateForm() throws Exception {

        mvc.perform(get("/borrowing-records/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("borrowingRecords/new"))
                .andExpect(content().string(containsString("Adding new borrowing record")))
                .andExpect(content().string(containsString("Add borrowing record")));
    }

    @Test
    void testCreate() throws Exception {

        // errors
        mvc.perform(post("/borrowing-records")
                        .param("book", "")
                        .param("visitor", "")
                        .param("borrowedAt", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("borrowingRecords/new"));

        verify(borrowingRecordService, never()).create(any(BorrowingRecord.class));

        // book is not free
        var book = testDataProvider.getBooks().stream().filter(b -> !b.isFree()).findAny().orElse(null);
        assertNotNull(book);
        mvc.perform(post("/borrowing-records")
                        .param("book", book.getFullName())
                        .param("visitor", newBorrowingRecord.getVisitor().getName())
                        .param("borrowedAt", newBorrowingRecord.getBorrowedAt().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verify(borrowingRecordService, never()).create(any(BorrowingRecord.class));

        // is ok
        assertNotNull(book);
        mvc.perform(post("/borrowing-records")
                        .param("book", newBorrowingRecord.getBook().getFullName())
                        .param("visitor", newBorrowingRecord.getVisitor().getName())
                        .param("borrowedAt", newBorrowingRecord.getBorrowedAt().toString()))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/borrowing-records"));

        verify(borrowingRecordService, times(1)).create(any(BorrowingRecord.class));
    }

    @Test
    void testUpdateForm() throws Exception {

        var borrowingRecords = testDataProvider.getBorrowingRecords();
        assertFalse(borrowingRecords.isEmpty());

        for (var borrowingRecord : borrowingRecords) {

            mvc.perform(get("/borrowing-records/{id}/edit", borrowingRecord.getId()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("borrowingRecords/edit"))
                    .andExpect(model().attribute("borrowingRecord", borrowingRecord))
                    .andExpect(content().string(containsString("Editing borrowing record")))
                    .andExpect(content().string(containsString("Update borrowing record")));

            verify(borrowingRecordService, times(1)).getById(borrowingRecord.getId());
        }

        mvc.perform(get("/borrowing-records/{id}/edit", -1L))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verify(borrowingRecordService, times(1)).getById(-1L);
    }

    @Test
    void testUpdate() throws Exception {

        var borrowingRecords = testDataProvider.getBorrowingRecords();
        assertFalse(borrowingRecords.isEmpty());

        // errors
        mvc.perform(post("/borrowing-records/{id}", -1L)
                        .param("book", newBorrowingRecord.getBook().getFullName())
                        .param("visitor", newBorrowingRecord.getVisitor().getName())
                        .param("borrowedAt", newBorrowingRecord.getBorrowedAt().toString())
                        .param("returnedAt", Objects.requireNonNullElse(newBorrowingRecord.getReturnedAt(), "").toString())
                        .param("user", newBorrowingRecord.getUser().getUsername()))
                .andExpect(status().isOk())
                .andExpect(view().name("error")); // id is not found

        for (var borrowingRecord : borrowingRecords) {

            // errors
            mvc.perform(post("/borrowing-records/{id}", borrowingRecord.getId())
                            .param("book", "")
                            .param("visitor", "")
                            .param("borrowedAt", "")
                            .param("returnedAt", "")
                            .param("user", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("borrowingRecords/edit"));

            // is ok
            mvc.perform(post("/borrowing-records/{id}", borrowingRecord.getId())
                            .param("book", borrowingRecord.getBook().getFullName())
                            .param("visitor", borrowingRecord.getVisitor().getName())
                            .param("borrowedAt", borrowingRecord.getBorrowedAt().toString())
                            .param("returnedAt", Objects.requireNonNullElse(borrowingRecord.getReturnedAt(), "").toString())
                            .param("user", borrowingRecord.getUser().getUsername()))
                    .andExpect(status().isFound())
                    .andExpect(view().name("redirect:/borrowing-records/" + borrowingRecord.getId()));
        }

        verify(borrowingRecordService, times(borrowingRecords.size() + 1)).update(anyLong(), any(BorrowingRecord.class));
    }

    @Test
    void testReturnBook() throws Exception {

        // error
        mvc.perform(patch("/borrowing-records/{id}/return", -1L))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verify(borrowingRecordService, never()).update(anyLong(), any(BorrowingRecord.class));

        var borrowingRecords = testDataProvider.getBorrowingRecords();
        assertFalse(borrowingRecords.isEmpty());

        var countOfSuccess = 0;

        for (var borrowingRecord : borrowingRecords) {

            if (!borrowingRecord.isBorrowedNow()) {

                // error
                mvc.perform(patch("/borrowing-records/{id}/return", borrowingRecord.getId()))
                        .andExpect(status().isOk())
                        .andExpect(view().name("error"));
            } else {

                // is ok
                mvc.perform(patch("/borrowing-records/{id}/return", borrowingRecord.getId()))
                        .andExpect(status().isFound())
                        .andExpect(view().name("redirect:/borrowing-records/" + borrowingRecord.getId()));

                countOfSuccess++;
            }
        }

        verify(borrowingRecordService, times(countOfSuccess)).update(anyLong(), any(BorrowingRecord.class));
    }

    @Test
    void testDelete() throws Exception {

        var borrowingRecords = testDataProvider.getBorrowingRecords();
        assertFalse(borrowingRecords.isEmpty());

        for (var borrowingRecord : borrowingRecords) {

            mvc.perform(delete("/borrowing-records/{id}", borrowingRecord.getId()))
                    .andExpect(status().isFound())
                    .andExpect(view().name("redirect:/borrowing-records"));

            verify(borrowingRecordService, times(1)).deleteById(borrowingRecord.getId());
        }
    }
}