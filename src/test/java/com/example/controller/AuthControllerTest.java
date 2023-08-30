package com.example.controller;

import com.example.TestDataProvider;
import com.example.model.Role;
import com.example.model.User;
import com.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TestDataProvider testDataProvider;
    
    private User newUser;
    
    @BeforeEach
    void setUp() {

        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        newUser = new User(1, "1 new test user", "password", true, Set.of(Role.USER));

        Mockito.reset(userService, passwordEncoder);

        // userService
        when(userService.getByUsername("")).thenReturn(null);
        when(userService.create(newUser)).thenReturn(newUser);
        for (var user : testDataProvider.getUsers()) {
            when(userService.getByUsername(user.getUsername())).thenReturn(user);
        }

        // passwordEncoder
        when(passwordEncoder.encode(anyString())).thenReturn("password");
    }

    @Test
    void testLoginForm() throws Exception {
        mvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(content().string(containsString("Log in")));
    }

    @Test
    void testRegisterForm() throws Exception {
        mvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(content().string(containsString("Register")));
    }

    @Test
    void testRegister() throws Exception {

        // error
        mvc.perform(post("/auth")
                        .param("username", "")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));

        verify(userService, never()).create(any(User.class));

        // error
        mvc.perform(post("/auth")
                        .param("username", testDataProvider.getUsers().get(0).getUsername())
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("error")); // username is not available

        verify(userService, never()).create(any(User.class));

        // is ok
        mvc.perform(post("/auth")
                        .param("username", newUser.getUsername())
                        .param("password", newUser.getPassword()))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/auth/login"));

        verify(userService, times(1)).create(any(User.class));
        verify(passwordEncoder, times(1)).encode(anyString());
    }
}