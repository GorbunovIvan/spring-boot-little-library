package com.example.controller;

import com.example.model.Author;
import com.example.model.Book;
import com.example.model.User;
import com.example.security.SecurityUtils;
import com.example.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String getAll(Model model) {
        model.addAttribute("books", bookService.getAll());
        return "books/books";
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable int id, Model model) {
        var book = bookService.getById(id);
        if (book == null) {
            throw new RuntimeException("Book with id '" + id + "' is not found");
        }
        model.addAttribute("book", book);
        return "books/book";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        var book = new Book();
        model.addAttribute("book", book);
        return "books/new";
    }

    @PostMapping
    public String create(Model model,
                         @ModelAttribute @Valid Book book, BindingResult bindingResult,
                         @RequestParam(name = "author0", required = false) Author author0,
                         @RequestParam(name = "author1", required = false) Author author1,
                         @RequestParam(name = "author2", required = false) Author author2) {

        setAuthorsToBook(book, bindingResult, author0, author1, author2);

        if (bindingResult.hasErrors()) {
            model.addAttribute("book", book);
            return "books/new";
        }
        bookService.create(book);
        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    public String updateForm(@PathVariable int id, Model model) {
        var book = bookService.getById(id);
        if (book == null) {
            throw new RuntimeException("Book with id '" + id + "' is not found");
        }
        model.addAttribute("book", book);
        return "books/edit";
    }

    @PostMapping("/{id}")
    public String update(Model model, @PathVariable int id,
                         @ModelAttribute @Valid Book book, BindingResult bindingResult,
                         @RequestParam(name = "author0", required = false) Author author0,
                         @RequestParam(name = "author1", required = false) Author author1,
                         @RequestParam(name = "author2", required = false) Author author2) {

        setAuthorsToBook(book, bindingResult, author0, author1, author2);

        if (bindingResult.hasErrors()) {
            model.addAttribute("book", book);
            return "books/edit";
        }
        bookService.update(id, book);
        return "redirect:/books/" + id;
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id) {
        bookService.deleteById(id);
        return "redirect:/books";
    }

    private void setAuthorsToBook(Book book, BindingResult bindingResult, Author... authors) {

        var authorsCollection = Arrays.stream(authors)
                .distinct()
                .filter(Objects::nonNull)
                .filter(author -> !author.getName().isBlank())
                .collect(Collectors.toSet());

        if (authorsCollection.isEmpty()) {
            bindingResult.rejectValue("authors", "no author");
        } else {
            book.setAuthors(authorsCollection);
        }
    }

    @ModelAttribute("currentUser")
    private User currentUser() {
        return securityUtils.getCurrentUser();
    }
}
