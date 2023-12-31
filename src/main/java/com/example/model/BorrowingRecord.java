package com.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "borrowing-records",
        uniqueConstraints = @UniqueConstraint(columnNames = { "book_id", "visitor_id", "borrowed_at" }))
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode(of = { "book", "visitor", "borrowedAt" })
public class BorrowingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "book_id")
    @NotNull(message = "book is empty")
    private Book book;

    @ManyToOne(cascade = { CascadeType.REFRESH })
    @JoinColumn(name = "visitor_id")
    @NotNull(message = "visitor is empty")
    private Visitor visitor;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "borrowed_at")
    @NotNull
    private LocalDateTime borrowedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    private static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PrePersist
    private void init() {
        if (borrowedAt == null) {
            borrowedAt = LocalDateTime.now();
        }
    }

    @PreRemove
    private void remove() {
        visitor.getBorrowingRecords().remove(this);
        book.getBorrowingRecords().remove(this);
    }

    public boolean isBorrowedNow() {
        return getReturnedAt() == null;
    }

    public void returnBook() {
        setReturnedAt(LocalDateTime.now());
    }

    public String toString() {
        return String.format("%s taken by %s at %s and %s",
                getBook() != null ? getBook().getFullName() : "",
                getVisitor() != null ? getVisitor().getName() : "",
                formatDateTime(getBorrowedAt()),
                isBorrowedNow() ? "not returned yet" : "returned at " + formatDateTime(getReturnedAt()));
    }

    public String getNameForBookPage() {
        return String.format("%s at %s and %s",
                getVisitor() != null ? getVisitor().getName() : "",
                formatDateTime(getBorrowedAt()),
                isBorrowedNow() ? "not returned yet" : "returned at " + formatDateTime(getReturnedAt()));
    }

    public String getNameForVisitorPage() {
        return String.format("%s (taken at %s and %s)",
                getBook() != null ? getBook().getFullName() : "",
                formatDateTime(getBorrowedAt()),
                isBorrowedNow() ? "not returned yet" : "returned at " + formatDateTime(getReturnedAt()));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }
}
