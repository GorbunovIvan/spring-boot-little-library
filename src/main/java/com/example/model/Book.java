package com.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode(of = { "name", "authors" })
@ToString
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    @NotNull
    @Size(min = 1, max = 199, message = "name should be not in range from 3 to 99 characters long")
    private String name;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @ToString.Exclude
    private Set<Author> authors = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @ToString.Exclude
    private Set<BorrowingRecord> borrowingRecords = new HashSet<>();

    public BorrowingRecord getCurrentBorrowingRecord() {
        return borrowingRecords.stream()
                .filter(BorrowingRecord::isBorrowed)
                .findAny()
                .orElse(null);
    }

    public Visitor getHolder() {
        var borrowingRecord = getCurrentBorrowingRecord();
        if (borrowingRecord == null) {
            return null;
        }
        return borrowingRecord.getVisitor();
    }

    public boolean isFree() {
        return getCurrentBorrowingRecord() == null;
    }
}
