package com.example.repository;

import com.example.model.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface VisitorRepository extends JpaRepository<Visitor, Integer> {

    @Query("FROM Visitor visitors " +
            "LEFT JOIN FETCH visitors.borrowingRecords borrowingRecords " +
            "LEFT JOIN FETCH borrowingRecords.book books " +
            "LEFT JOIN FETCH books.authors authors " +
            "WHERE visitors.id = :id")
    @NonNull
    Optional<Visitor> findById(@Param("id") @Nullable Integer id);

    @Query("FROM Visitor visitors " +
            "LEFT JOIN FETCH visitors.borrowingRecords borrowingRecords " +
            "LEFT JOIN FETCH borrowingRecords.book books " +
            "LEFT JOIN FETCH books.authors authors " +
            "WHERE UPPER(visitors.name) = UPPER(:name)")
    @NonNull
    Optional<Visitor> findByNameIgnoreCase(@Param("name") @Nullable String name);
}
