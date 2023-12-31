package com.example.controller.converter;

import com.example.model.Author;
import com.example.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthorConverter implements Converter<String, Author> {

    private final BookService bookService;

    @Override
    public Author convert(@Nullable String source) {
        if (Objects.requireNonNullElse(source, "").isBlank()) {
            return null;
        }
        var author = bookService.getAuthorByName(source);
        if (author != null) {
            return author;
        }
        return new Author(source);
    }
}
