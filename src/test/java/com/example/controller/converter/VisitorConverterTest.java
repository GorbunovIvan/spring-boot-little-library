package com.example.controller.converter;

import com.example.TestDataProvider;
import com.example.model.Visitor;
import com.example.service.VisitorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class VisitorConverterTest {

    @Autowired
    private VisitorConverter visitorConverter;

    @MockBean
    private VisitorService visitorService;

    @Autowired
    private TestDataProvider testDataProvider;

    @BeforeEach
    void setUp() {
        when(visitorService.getByName("")).thenReturn(null);
        when(visitorService.create(any(Visitor.class))).thenReturn(new Visitor());
        for (var visitor : testDataProvider.getVisitors()) {
            when(visitorService.getByName(visitor.getName())).thenReturn(visitor);
        }
    }

    @Test
    void testConvert() {

        var visitors = testDataProvider.getVisitors();
        assertFalse(visitors.isEmpty());

        for (var visitor : visitors) {
            assertEquals(visitor, visitorConverter.convert(visitor.getName()));
            verify(visitorService, times(1)).getByName(visitor.getName());
            verify(visitorService, never()).create(any(Visitor.class));
        }

        assertNull(visitorConverter.convert(""));
        verify(visitorService, never()).getByName("");
        verify(visitorService, never()).create(any(Visitor.class));

        // 'visitorConverter.convert()' must create new if it does not find
        assertNotNull(visitorConverter.convert("---"));
        verify(visitorService, times(1)).getByName("---");
        verify(visitorService, times(1)).create(any(Visitor.class));
    }
}