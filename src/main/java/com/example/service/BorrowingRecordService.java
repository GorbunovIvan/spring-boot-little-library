package com.example.service;

import com.example.model.BorrowingRecord;
import com.example.repository.BorrowingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BorrowingRecordService {

    private final BorrowingRecordRepository borrowingRecordRepository;

    public BorrowingRecord getById(Long id) {
        return borrowingRecordRepository.findById(id)
                .orElse(null);
    }

    public Set<BorrowingRecord> getAll() {
        return new HashSet<>(borrowingRecordRepository.findAll());
    }

    public BorrowingRecord create(BorrowingRecord borrowingRecord) {
        return borrowingRecordRepository.save(borrowingRecord);
    }

    @Transactional
    public BorrowingRecord update(Long id, BorrowingRecord borrowingRecord) {
        if (!borrowingRecordRepository.existsById(id)) {
            throw new RuntimeException("Borrowing record with id '" + id + "' is not found");
        }
        borrowingRecord.setId(id);
        return borrowingRecordRepository.save(borrowingRecord);
    }

    @Transactional
    public void deleteById(Long id) {
        borrowingRecordRepository.deleteById(id);
    }
}
