package com.example.repository;

import com.example.model.BorrowingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {

    @Query("FROM BorrowingRecord borrowingRecords " +
            "LEFT JOIN FETCH borrowingRecords.book books " +
            "LEFT JOIN FETCH borrowingRecords.visitor visitors " +
            "LEFT JOIN FETCH borrowingRecords.user users " +
            "LEFT JOIN FETCH books.authors authors " +
            "LEFT JOIN FETCH users.roles roles")
    @NonNull
    List<BorrowingRecord> findAll();

    @Query("FROM BorrowingRecord borrowingRecords " +
            "LEFT JOIN FETCH borrowingRecords.book books " +
            "LEFT JOIN FETCH borrowingRecords.visitor visitors " +
            "LEFT JOIN FETCH borrowingRecords.user users " +
            "LEFT JOIN FETCH books.authors authors " +
            "LEFT JOIN FETCH users.roles roles " +
            "WHERE borrowingRecords.id = :id")
    @NonNull
    Optional<BorrowingRecord> findById(@Param("id") @Nullable Long id);
}
