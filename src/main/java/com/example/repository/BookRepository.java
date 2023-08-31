package com.example.repository;

import com.example.model.Author;
import com.example.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer>, BookRepositoryCustom {

    @Query("FROM Book books " +
            "LEFT JOIN FETCH books.authors " +
            "LEFT JOIN FETCH books.borrowingRecords borrowingRecords " +
            "LEFT JOIN FETCH borrowingRecords.visitor visitors " +
            "WHERE books.id = :id")
    @NonNull
    Optional<Book> findById(@Param("id") @Nullable Integer id);

    @Query("FROM Book books " +
            "LEFT JOIN FETCH books.authors " +
            "LEFT JOIN FETCH books.borrowingRecords borrowingRecords " +
            "LEFT JOIN FETCH borrowingRecords.visitor visitors")
    @NonNull
    List<Book> findAll();

    @Query("FROM Book books " +
            "LEFT JOIN FETCH books.authors " +
            "LEFT JOIN FETCH books.borrowingRecords borrowingRecords " +
            "LEFT JOIN FETCH borrowingRecords.visitor visitors " +
            "WHERE UPPER(books.name) = UPPER(:name)")
    @NonNull
    List<Book> findAllByNameIgnoreCase(@Param("name") String name);

    @Query("FROM Author WHERE name = :name")
    Optional<Author> findAuthorByName(@Param("name") String name);
}
