package com.example.controller;

import com.example.model.BorrowingRecord;
import com.example.model.User;
import com.example.security.SecurityUtils;
import com.example.service.BookService;
import com.example.service.BorrowingRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/borrowing-records")
@RequiredArgsConstructor
public class BorrowingRecordController {

    private final BorrowingRecordService borrowingRecordService;
    private final BookService bookService;

    private final SecurityUtils securityUtils;

    @GetMapping
    public String getAll(Model model) {
        model.addAttribute("borrowingRecords", borrowingRecordService.getAll());
        return "borrowingRecords/borrowingRecords";
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable long id, Model model) {
        var borrowingRecord = borrowingRecordService.getById(id);
        if (borrowingRecord == null) {
            throw new RuntimeException("Borrowing record with id '" + id + "' is not found");
        }
        model.addAttribute("borrowingRecord", borrowingRecord);
        return "borrowingRecords/borrowingRecord";
    }

    @GetMapping("/new")
    public String createForm(Model model,
                             @RequestParam(value = "bookId", required = false) Integer bookId) {

        var borrowingRecord = new BorrowingRecord();
        borrowingRecord.setBorrowedAt(LocalDateTime.now());

        if (bookId != null) {
            borrowingRecord.setBook(bookService.getById(bookId));
        }

        model.addAttribute("borrowingRecord", borrowingRecord);
        return "borrowingRecords/new";
    }

    @PostMapping
    public String create(Model model,
                         @ModelAttribute @Valid BorrowingRecord borrowingRecord, BindingResult bindingResult) {

        if (borrowingRecord.getBook() != null && !borrowingRecord.getBook().isFree()) {
            throw new RuntimeException("Book '" + borrowingRecord.getBook().getFullName() + "' is not available");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("borrowingRecord", borrowingRecord);
            return "borrowingRecords/new";
        }

        borrowingRecord.setUser(currentUser());
        borrowingRecordService.create(borrowingRecord);

        return "redirect:/borrowing-records";
    }

    @GetMapping("/{id}/edit")
    public String updateForm(@PathVariable long id, Model model) {
        var borrowingRecord = borrowingRecordService.getById(id);
        if (borrowingRecord == null) {
            throw new RuntimeException("Borrowing record with id '" + id + "' is not found");
        }
        model.addAttribute("borrowingRecord", borrowingRecord);
        return "borrowingRecords/edit";
    }

    @PostMapping("/{id}")
    public String update(Model model, @PathVariable long id,
                         @ModelAttribute @Valid BorrowingRecord borrowingRecord, BindingResult bindingResult) {

        if (borrowingRecord.getBook() != null && !borrowingRecord.getBook().isFree()) {
            if (borrowingRecord.getReturnedAt() == null) {
                if (!borrowingRecord.getBook().getCurrentBorrowingRecord().getId().equals(id)) {
                    throw new RuntimeException("Book '" + borrowingRecord.getBook().getFullName() + "' is not available");
                }
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("borrowingRecord", borrowingRecord);
            return "borrowingRecords/edit";
        }
        borrowingRecordService.update(id, borrowingRecord);
        return "redirect:/borrowing-records/" + id;
    }

    @PatchMapping("/{id}/return")
    public String returnBook(@PathVariable long id) {
        var borrowingRecord = borrowingRecordService.getById(id);
        if (borrowingRecord == null) {
            throw new RuntimeException("Borrowing record with id '" + id + "' is not found");
        }
        if (!borrowingRecord.isBorrowedNow()) {
            throw new RuntimeException("Book was already returned at " + borrowingRecord.getReturnedAt());
        }
        borrowingRecord.returnBook();
        borrowingRecordService.update(id, borrowingRecord);
        return "redirect:/borrowing-records/" + id;
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable long id) {
        borrowingRecordService.deleteById(id);
        return "redirect:/borrowing-records";
    }

    @ModelAttribute("currentUser")
    private User currentUser() {
        return securityUtils.getCurrentUser();
    }
}
