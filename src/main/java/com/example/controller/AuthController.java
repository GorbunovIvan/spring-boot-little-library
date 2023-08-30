package com.example.controller;

import com.example.model.Role;
import com.example.model.User;
import com.example.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping
    public String register(Model model,
                           @ModelAttribute @Valid User user, BindingResult bindingResult) {

        if (userService.getByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Username '" + user.getUsername() + "' is not available");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "auth/register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.getRoles().add(Role.USER);
        user.setIsActive(true);

        userService.create(user);

        return "redirect:/auth/login";
    }
}
