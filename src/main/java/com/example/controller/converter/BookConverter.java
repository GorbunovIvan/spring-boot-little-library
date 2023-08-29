package com.example.controller.converter;

import com.example.model.Author;
import com.example.model.Book;
import com.example.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookConverter implements Converter<String, Book> {

    private final BookService bookService;

    @Override
    public Book convert(@Nullable String source) {

        if (Objects.requireNonNullElse(source, "").isBlank()) {
            return null;
        }

        var books = bookService.getAllByName(source);
        if (!books.isEmpty()) {
            if (books.size() > 1) {
                log.info(books.size() + " books with name '" + source + "' were found!! First of them is chosen");
            }
            return books.get(0);
        }

        var matcher = Pattern.compile("'(.+)' by (.+)").matcher(source);
        if (!matcher.find()) {
            matcher = Pattern.compile("(.+) by (.+)").matcher(source);
            if (!matcher.find()) {
                return null;
            }
        }

        var name = matcher.group(1);

        var booksByName = bookService.getAllByName(name);
        if (booksByName.isEmpty()) {
            return null;
        }

        var authorsString = matcher.group(2);
        for (var book : booksByName) {
            if (book.getAuthorsAsString().equals(authorsString)) {
                return book;
            }
        }

        var authorsNames = Arrays.stream(authorsString.split(", ")).collect(Collectors.toSet());

        for (var book : booksByName) {
            var allAuthorsMatch = book.getAuthors().stream()
                    .map(Author::getName)
                    .collect(Collectors.toSet()).equals(authorsNames);
            if (allAuthorsMatch) {
                return book;
            }
        }

        for (var book : booksByName) {
            for (var authorName : authorsNames) {
                if (book.getAuthors().stream().anyMatch(a -> a.getName().equalsIgnoreCase(authorName))) {
                    return book;
                }
            }
        }

        if (booksByName.size() > 1) {
            log.info(books.size() + " books with name '" + source + "' were found!! First of them is chosen");
        }

        return books.get(0);
    }
}
