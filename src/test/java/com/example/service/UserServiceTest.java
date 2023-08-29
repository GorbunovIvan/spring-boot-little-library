package com.example.service;

import com.example.TestDataProvider;
import com.example.model.Role;
import com.example.model.User;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;
    
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private TestDataProvider testDataProvider;

    private User newUser;

    @BeforeEach
    void setUp() {

        newUser = new User(null, "1 new test visitor", "password", true, Set.of(Role.USER));

        Mockito.reset(userRepository);

        when(userRepository.findByUsername("")).thenReturn(Optional.empty());
        when(userRepository.save(newUser)).thenReturn(newUser);

        for (var user : testDataProvider.getUsers()) {
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
        }
    }

    @Test
    void testGetByUsername() {

        var users = testDataProvider.getUsers();
        assertFalse(users.isEmpty());

        for (var user : users) {
            assertEquals(user, userService.getByUsername(user.getUsername()));
            verify(userRepository, times(1)).findByUsername(user.getUsername());
        }

        assertNull(userService.getByUsername(""));
    }

    @Test
    void testLoadUserByUsername() {

        var users = testDataProvider.getUsers();
        assertFalse(users.isEmpty());

        for (var user : users) {
            assertEquals(user, userService.loadUserByUsername(user.getUsername()));
            verify(userRepository, times(1)).findByUsername(user.getUsername());
        }

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(""));
    }

    @Test
    void testCreate() {
        assertEquals(newUser, userService.create(newUser));
        verify(userRepository, times(1)).save(newUser);
    }
}