package com.example.repository;

import com.example.model.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VisitorRepository extends JpaRepository<Visitor, Integer> {

    Optional<Visitor> findByNameIgnoreCase(String name);
}
