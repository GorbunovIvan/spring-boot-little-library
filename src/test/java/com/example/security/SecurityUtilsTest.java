package com.example.security;

import com.example.TestDataProvider;
import com.example.model.User;
import com.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@SpringBootTest
class SecurityUtilsTest {

    @Autowired
    private SecurityUtils securityUtils;

    @MockBean
    private UserService userService;
    @MockBean
    private SecurityContext securityContext;
    @MockBean
    private Authentication authentication;

    @Autowired
    private TestDataProvider testDataProvider;

    private User currentUser;

    @BeforeEach
    void setUp() {

        currentUser = testDataProvider.getUsers().get(0);

        Mockito.reset(userService);

        when(userService.getByUsername("")).thenReturn(null);
        for (var user : testDataProvider.getUsers()) {
            when(userService.getByUsername(user.getUsername())).thenReturn(user);
        }

        when(authentication.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getCurrentUser() {

        // unauthorized
        assertNull(securityUtils.getCurrentUser());
        verify(securityContext, times(1)).getAuthentication();
        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, never()).getPrincipal();
        verify(userService, never()).getByUsername(anyString());

        // authorized
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(currentUser);

        assertEquals(currentUser, securityUtils.getCurrentUser());
        verify(securityContext, times(2)).getAuthentication();
        verify(authentication, times(2)).isAuthenticated();
        verify(authentication, times(1)).getPrincipal();
    }
}