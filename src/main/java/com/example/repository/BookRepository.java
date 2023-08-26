package com.example.repository;

import com.example.model.Author;
import com.example.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer>, BookRepositoryCustom {

    @Query("FROM Author WHERE name = :name")
    Optional<Author> findAuthorByName(@Param("name") String name);
}
