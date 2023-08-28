package com.example.controller.converter;

import com.example.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserConverter implements Converter<String, User> {

//    private final UserService userService;

    @Override
    public User convert(@Nullable String source) {
        if (Objects.requireNonNullElse(source, "").isBlank()) {
            return null;
        }
        return null;
//        return userService.getByName(source);
    }
}
