package com.example.controller.converter;

import com.example.TestDataProvider;
import com.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserConverterTest {

    @Autowired
    private UserConverter userConverter;

    @MockBean
    private UserService userService;

    @Autowired
    private TestDataProvider testDataProvider;

    @BeforeEach
    void setUp() {
        when(userService.getByUsername("")).thenReturn(null);
        for (var user : testDataProvider.getUsers()) {
            when(userService.getByUsername(user.getUsername())).thenReturn(user);
        }
    }

    @Test
    void testConvert() {

        var users = testDataProvider.getUsers();
        assertFalse(users.isEmpty());

        for (var user : users) {
            assertEquals(user, userConverter.convert(user.getUsername()));
            verify(userService, times(1)).getByUsername(user.getUsername());
        }

        assertNull(userConverter.convert(""));
        verify(userService, never()).getByUsername("");

        assertNull(userConverter.convert("---"));
        verify(userService, times(1)).getByUsername("---");
    }
}