package com.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Entity
@Table(name = "visitors")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode(of = { "name" })
@ToString
public class Visitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    @NotNull
    @Size(min = 3, max = 99, message = "name should be in range from 3 to 99 characters long")
    private String name;

    @OneToMany(mappedBy = "visitor", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @OrderBy("borrowedAt")
    @ToString.Exclude
    private Set<BorrowingRecord> borrowingRecords = new TreeSet<>(Comparator.comparing(BorrowingRecord::getBorrowedAt));

    public Set<Book> getHeldBooks() {
        return borrowingRecords.stream()
                .filter(BorrowingRecord::isBorrowed)
                .map(BorrowingRecord::getBook)
                .collect(Collectors.toSet());
    }
}
