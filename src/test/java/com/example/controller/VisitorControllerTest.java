package com.example.controller;

import com.example.TestDataProvider;
import com.example.model.Visitor;
import com.example.security.SecurityUtils;
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

import java.util.HashSet;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VisitorControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VisitorService visitorService;

    @MockBean
    private SecurityUtils securityUtils;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TestDataProvider testDataProvider;
    
    private Visitor newVisitor;
    
    @BeforeEach
    void setUp() {

        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        newVisitor = new Visitor(null, "1 new test visitor", new HashSet<>());

        Mockito.reset(visitorService, securityUtils);

        // visitorService
        when(visitorService.getAll()).thenReturn(new HashSet<>(testDataProvider.getVisitors()));
        when(visitorService.getById(-1)).thenReturn(null);
        when(visitorService.create(newVisitor)).thenReturn(newVisitor);
        when(visitorService.update(-1, newVisitor)).thenThrow(RuntimeException.class);
        when(visitorService.update(null, newVisitor)).thenThrow(RuntimeException.class);
        doNothing().when(visitorService).deleteById(anyInt());

        for (var visitor : testDataProvider.getVisitors()) {
            when(visitorService.getById(visitor.getId())).thenReturn(visitor);
            when(visitorService.update(visitor.getId(), visitor)).thenReturn(visitor);
        }
    }

    @Test
    void testGetAll() throws Exception {

        var visitors = new HashSet<>(testDataProvider.getVisitors());
        assertFalse(visitors.isEmpty());

        String result = mvc.perform(get("/visitors"))
                .andExpect(status().isOk())
                .andExpect(view().name("visitors/visitors"))
                .andExpect(model().attribute("visitors", visitors))
                .andReturn()
                .getResponse()
                .getContentAsString();

        for (var visitor : visitors) {
            assertTrue(result.contains(visitor.getName()));
        }

        verify(visitorService, times(1)).getAll();
    }

    @Test
    void testGetById() throws Exception {

        var visitors = testDataProvider.getVisitors();
        assertFalse(visitors.isEmpty());

        for (var visitor : visitors) {

            mvc.perform(get("/visitors/{id}", visitor.getId()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("visitors/visitor"))
                    .andExpect(model().attribute("visitor", visitor))
                    .andExpect(content().string(containsString(visitor.getName())));

            verify(visitorService, times(1)).getById(visitor.getId());
        }

        mvc.perform(get("/visitors/{id}", -1))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verify(visitorService, times(1)).getById(-1);
    }

    @Test
    void testCreateForm() throws Exception {

        mvc.perform(get("/visitors/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("visitors/new"))
                .andExpect(model().attribute("visitor", new Visitor()))
                .andExpect(content().string(containsString("Adding new visitor")))
                .andExpect(content().string(containsString("Add visitor")));
    }

    @Test
    void testCreate() throws Exception {

        // errors
        mvc.perform(post("/visitors")
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("visitors/new"));

        verify(visitorService, never()).create(any(Visitor.class));

        // is ok
        mvc.perform(post("/visitors")
                        .param("name", newVisitor.getName()))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/visitors"));

        verify(visitorService, times(1)).create(any(Visitor.class));
    }

    @Test
    void testUpdateForm() throws Exception {

        var visitors = testDataProvider.getVisitors();
        assertFalse(visitors.isEmpty());

        for (var visitor : visitors) {

            mvc.perform(get("/visitors/{id}/edit", visitor.getId()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("visitors/edit"))
                    .andExpect(model().attribute("visitor", visitor))
                    .andExpect(content().string(containsString("Editing visitor")))
                    .andExpect(content().string(containsString("Update visitor")));

            verify(visitorService, times(1)).getById(visitor.getId());
        }

        mvc.perform(get("/visitors/{id}/edit", -1))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verify(visitorService, times(1)).getById(-1);
    }

    @Test
    void testUpdate() throws Exception {

        var visitors = testDataProvider.getVisitors();
        assertFalse(visitors.isEmpty());

        // errors
        mvc.perform(post("/visitors/{id}", -1)
                        .param("name", newVisitor.getName()))
                .andExpect(status().isOk())
                .andExpect(view().name("error")); // id is not found

        for (var visitor : visitors) {

            // errors
            mvc.perform(post("/visitors/{id}", visitor.getId())
                            .param("name", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("visitors/edit"));

            // is ok
            mvc.perform(post("/visitors/{id}", visitor.getId())
                            .param("name", visitor.getName()))
                    .andExpect(status().isFound())
                    .andExpect(view().name("redirect:/visitors/" + visitor.getId()));
        }

        verify(visitorService, times(visitors.size() + 1)).update(anyInt(), any(Visitor.class));
    }

    @Test
    void testDelete() throws Exception {

        var visitors = testDataProvider.getVisitors();
        assertFalse(visitors.isEmpty());

        for (var visitor : visitors) {

            mvc.perform(delete("/visitors/{id}", visitor.getId()))
                    .andExpect(status().isFound())
                    .andExpect(view().name("redirect:/visitors"));

            verify(visitorService, times(1)).deleteById(visitor.getId());
        }
    }
}