package com.example.service;

import com.example.TestDataProvider;
import com.example.model.Visitor;
import com.example.repository.VisitorRepository;
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
class VisitorServiceTest {

    @Autowired
    private VisitorService visitorService;
    
    @MockBean
    private VisitorRepository visitorRepository;

    @Autowired
    private TestDataProvider testDataProvider;

    private Visitor newVisitor;

    @BeforeEach
    void setUp() {

        newVisitor = new Visitor(null, "1 new test visitor", new HashSet<>());

        Mockito.reset(visitorRepository);

        when(visitorRepository.findAll()).thenReturn(testDataProvider.getVisitors());
        when(visitorRepository.findById(-1)).thenReturn(Optional.empty());
        when(visitorRepository.findByNameIgnoreCase("")).thenReturn(Optional.empty());
        when(visitorRepository.existsById(-1)).thenReturn(false);
        when(visitorRepository.save(newVisitor)).thenReturn(newVisitor);
        doNothing().when(visitorRepository).deleteById(anyInt());

        for (var visitor : testDataProvider.getVisitors()) {
            when(visitorRepository.findById(visitor.getId())).thenReturn(Optional.of(visitor));
            when(visitorRepository.findByNameIgnoreCase(visitor.getName())).thenReturn(Optional.of(visitor));
            when(visitorRepository.existsById(visitor.getId())).thenReturn(true);
            when(visitorRepository.save(visitor)).thenReturn(visitor);
        }
    }

    @Test
    void testGetById() {

        var visitors = testDataProvider.getVisitors();
        assertFalse(visitors.isEmpty());

        for (var visitor : visitors) {
            assertEquals(visitor, visitorService.getById(visitor.getId()));
            verify(visitorRepository, times(1)).findById(visitor.getId());
        }

        assertNull(visitorService.getById(-1));
    }

    @Test
    void testGetByName() {

        var visitors = testDataProvider.getVisitors();
        assertFalse(visitors.isEmpty());

        for (var visitor : visitors) {
            assertEquals(visitor, visitorService.getByName(visitor.getName()));
            verify(visitorRepository, times(1)).findByNameIgnoreCase(visitor.getName());
        }

        assertNull(visitorService.getByName(""));
    }

    @Test
    void testGetAll() {

        var visitors = testDataProvider.getVisitors();
        assertFalse(visitors.isEmpty());

        assertEquals(new HashSet<>(visitors), visitorService.getAll());
        verify(visitorRepository, times(1)).findAll();
    }

    @Test
    void testCreate() {
        assertEquals(newVisitor, visitorService.create(newVisitor));
        verify(visitorRepository, times(1)).save(newVisitor);
    }

    @Test
    void testUpdate() {

        var visitors = testDataProvider.getVisitors();
        assertFalse(visitors.isEmpty());

        for (var visitor : visitors) {
            assertEquals(visitor, visitorService.update(visitor.getId(), visitor));
            verify(visitorRepository, times(1)).existsById(visitor.getId());
            verify(visitorRepository, times(1)).save(visitor);
        }

        assertThrows(RuntimeException.class, () -> visitorService.update(-1, newVisitor));
        verify(visitorRepository, times(1)).existsById(-1);
        verify(visitorRepository, never()).save(newVisitor);
    }

    @Test
    void testDeleteById() {

        var visitors = testDataProvider.getVisitors();
        assertFalse(visitors.isEmpty());

        for (var visitor : visitors) {
            visitorService.deleteById(visitor.getId());
            verify(visitorRepository, times(1)).deleteById(visitor.getId());
        }
    }
}