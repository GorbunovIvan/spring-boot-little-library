package com.example.service;

import com.example.model.Visitor;
import com.example.repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VisitorService {

    private final VisitorRepository visitorRepository;

    public Visitor getById(Integer id) {
        return visitorRepository.findById(id)
                .orElse(null);
    }

    public Visitor getByName(String name) {
        return visitorRepository.findByNameIgnoreCase(name)
                .orElse(null);
    }

    public Set<Visitor> getAll() {
        return new HashSet<>(visitorRepository.findAll());
    }

    public Visitor create(Visitor visitor) {
        return visitorRepository.save(visitor);
    }

    @Transactional
    public Visitor update(Integer id, Visitor visitor) {
        if (!visitorRepository.existsById(id)) {
            throw new RuntimeException("Visitor with id '" + id + "' is not found");
        }
        visitor.setId(id);
        return visitorRepository.save(visitor);
    }

    public void deleteById(Integer id) {
        visitorRepository.deleteById(id);
    }
}
