package com.example;

import com.example.model.*;
import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
@Getter
public class TestDataProvider {

    private final List<Book> books;
    private final List<Author> authors;
    private final List<Visitor> visitors;
    private final List<User> users;
    private final List<BorrowingRecord> borrowingRecords;

    public TestDataProvider() {

        authors = List.of(
                new Author(1, "1 test author", new HashSet<>()),
                new Author(2, "2 test author", new HashSet<>()),
                new Author(3, "3 test author", new HashSet<>())
        );

        books = List.of(
                new Book(1, "1 test book", 1, Set.of(authors.get(0)), new HashSet<>()),
                new Book(2, "2 test book", 2, Set.of(authors.get(0), authors.get(2)), new HashSet<>()),
                new Book(3, "3 test book", 3, Set.of(authors.get(1)), new HashSet<>()),
                new Book(4, "4 test book", 4, Set.of(authors.get(1)), new HashSet<>()),
                new Book(5, "5 test book", 5, Set.of(authors.get(2)), new HashSet<>()),
                new Book(6, "6 test book", 6, Set.of(authors.get(0), authors.get(1)), new HashSet<>())
        );

        visitors = List.of(
                new Visitor(1, "1 test visitor", new HashSet<>()),
                new Visitor(2, "2 test visitor", new HashSet<>()),
                new Visitor(3, "3 test visitor", new HashSet<>())
        );

        users = List.of(
                new User(1, "1 test user", "password", true, Set.of(Role.USER)),
                new User(2, "2 test user", "password", true, Set.of(Role.USER))
        );

        borrowingRecords = List.of(
                new BorrowingRecord(1L, books.get(0), visitors.get(0), users.get(0), LocalDateTime.now().minusSeconds(2), LocalDateTime.now().minusSeconds(1)),
                new BorrowingRecord(2L, books.get(0), visitors.get(1), users.get(1), LocalDateTime.now(), null),
                new BorrowingRecord(3L, books.get(1), visitors.get(0), users.get(0), LocalDateTime.now().minusSeconds(2), LocalDateTime.now().minusSeconds(1)),
                new BorrowingRecord(4L, books.get(2), visitors.get(1), users.get(0), LocalDateTime.now(), null),
                new BorrowingRecord(5L, books.get(3), visitors.get(2), users.get(1), LocalDateTime.now(), null),
                new BorrowingRecord(6L, books.get(4), visitors.get(2), users.get(1), LocalDateTime.now(), null)
        );

        configureRelations();
    }

    private void configureRelations() {

        for (var author : authors) {
            author.setBooks(books.stream()
                    .filter(b -> b.getAuthors().contains(author))
                    .collect(Collectors.toSet()));
        }

        for (var book : books) {
            book.setBorrowingRecords(borrowingRecords.stream()
                    .filter(br -> br.getBook().equals(book))
                    .collect(Collectors.toSet()));
        }

        for (var visitor : visitors) {
            visitor.setBorrowingRecords(borrowingRecords.stream()
                    .filter(br -> br.getVisitor().equals(visitor))
                    .collect(Collectors.toSet()));
        }
    }

    public void removeIds() {
        authors.forEach(a -> a.setId(null));
        books.forEach(b -> b.setId(null));
        visitors.forEach(v -> v.setId(null));
        users.forEach(u -> u.setId(null));
        borrowingRecords.forEach(br -> br.setId(null));
    }
}
