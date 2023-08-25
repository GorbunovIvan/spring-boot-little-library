package com.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "borrowing-records",
        uniqueConstraints = @UniqueConstraint(columnNames = { "book_id", "visitor_id", "borrowed_at" }))
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode(of = { "book", "visitor", "borrowedAt" })
@ToString
public class BorrowingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_id")
    @NotNull
    private Book book;

    @ManyToOne
    @JoinColumn(name = "visitor_id")
    @NotNull
    private Visitor visitor;

    @Column(name = "borrowed_at")
    @NotNull
    private LocalDateTime borrowedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    private void init() {
        if (borrowedAt == null) {
            borrowedAt = LocalDateTime.now();
        }
    }

    public boolean isBorrowed() {
        return getReturnedAt() == null;
    }
}
