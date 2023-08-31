package com.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Entity
@Table(name = "books")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode(of = { "name", "year", "authors" })
@ToString
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    @NotNull(message = "name is empty")
    @Size(min = 1, max = 199, message = "name should be in range from 3 to 99 characters long")
    private String name;

    @Column(name = "year")
    @NotNull(message = "year is empty")
    @Digits(integer = 4, fraction = 0)
    private Integer year;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @ToString.Exclude
    private Set<Author> authors = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @OrderBy("borrowedAt DESC")
    @ToString.Exclude
    private Set<BorrowingRecord> borrowingRecords = new TreeSet<>(Comparator.comparing(BorrowingRecord::getBorrowedAt));

    public BorrowingRecord getCurrentBorrowingRecord() {
        return borrowingRecords.stream()
                .filter(BorrowingRecord::isBorrowedNow)
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

    public String getAuthorsAsString() {
        return getAuthors().stream()
                .map(Author::getName)
                .collect(Collectors.joining(", "));
    }

    public String getFullName() {
        return getFullName(false);
    }

    public String getFullName(boolean withStatus) {

        StringBuilder strBuilder = new StringBuilder();

        if (withStatus) {
            strBuilder.append("(");
            strBuilder.append(isFree() ? "free" : "not free");
            strBuilder.append(") ");
        }

        strBuilder.append("'").append(getName()).append("'");
        strBuilder.append(" by ");

        var authorsString = getAuthorsAsString();
        strBuilder.append(authorsString);

        return strBuilder.toString();
    }
}
