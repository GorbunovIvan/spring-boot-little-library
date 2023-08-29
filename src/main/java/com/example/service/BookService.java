package com.example.service;

import com.example.model.Author;
import com.example.model.Book;
import com.example.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public Book getById(Integer id) {
        return bookRepository.findById(id)
                .orElse(null);
    }

    public Set<Book> getAll() {
        return new HashSet<>(bookRepository.findAll());
    }

    public List<Book> getAllByName(String name) {
        return bookRepository.findAllByNameIgnoreCase(name);
    }

    public Book create(Book book) {
        return bookRepository.merge(book);
    }

    @Transactional
    public Book update(Integer id, Book book) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Book with id '" + id + "' is not found");
        }
        book.setId(id);
        return bookRepository.merge(book);
    }

    public void deleteById(Integer id) {
        bookRepository.deleteById(id);
    }

    public Author getAuthorByName(String name) {
        return bookRepository.findAuthorByName(name)
                .orElse(null);
    }
}
