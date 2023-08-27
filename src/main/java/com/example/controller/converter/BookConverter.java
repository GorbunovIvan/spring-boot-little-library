package com.example.controller.converter;

import com.example.model.Book;
import com.example.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookConverter implements Converter<String, Book> {

    private final BookService bookService;

    @Override
    public Book convert(@Nullable String source) {
        System.out.println("here is book converter");
        return null;
    }
}
