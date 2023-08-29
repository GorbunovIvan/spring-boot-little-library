package com.example.service;

import com.example.TestDataProvider;
import com.example.model.BorrowingRecord;
import com.example.repository.BorrowingRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class BorrowingRecordServiceTest {

    @Autowired
    private BorrowingRecordService borrowingRecordService;
    
    @MockBean
    private BorrowingRecordRepository borrowingRecordRepository;

    @Autowired
    private TestDataProvider testDataProvider;

    private BorrowingRecord newBorrowingRecord;

    @BeforeEach
    void setUp() {

        newBorrowingRecord = new BorrowingRecord(null, testDataProvider.getBooks().get(0), testDataProvider.getVisitors().get(0), testDataProvider.getUsers().get(0), LocalDateTime.now(), null);

        Mockito.reset(borrowingRecordRepository);

        when(borrowingRecordRepository.findAll()).thenReturn(testDataProvider.getBorrowingRecords());
        when(borrowingRecordRepository.findById(-1L)).thenReturn(Optional.empty());
        when(borrowingRecordRepository.existsById(-1L)).thenReturn(false);
        when(borrowingRecordRepository.save(newBorrowingRecord)).thenReturn(newBorrowingRecord);
        doNothing().when(borrowingRecordRepository).deleteById(anyLong());

        for (var borrowingRecord : testDataProvider.getBorrowingRecords()) {
            when(borrowingRecordRepository.findById(borrowingRecord.getId())).thenReturn(Optional.of(borrowingRecord));
            when(borrowingRecordRepository.existsById(borrowingRecord.getId())).thenReturn(true);
            when(borrowingRecordRepository.save(borrowingRecord)).thenReturn(borrowingRecord);
        }
    }

    @Test
    void testGetById() {

        var borrowingRecords = testDataProvider.getBorrowingRecords();
        assertFalse(borrowingRecords.isEmpty());

        for (var borrowingRecord : borrowingRecords) {
            assertEquals(borrowingRecord, borrowingRecordService.getById(borrowingRecord.getId()));
            verify(borrowingRecordRepository, times(1)).findById(borrowingRecord.getId());
        }

        assertNull(borrowingRecordService.getById(-1L));
    }

    @Test
    void testGetAll() {

        var borrowingRecords = testDataProvider.getBorrowingRecords();
        assertFalse(borrowingRecords.isEmpty());

        assertEquals(new HashSet<>(borrowingRecords), borrowingRecordService.getAll());
        verify(borrowingRecordRepository, times(1)).findAll();
    }

    @Test
    void testCreate() {
        assertEquals(newBorrowingRecord, borrowingRecordService.create(newBorrowingRecord));
        verify(borrowingRecordRepository, times(1)).save(newBorrowingRecord);
    }

    @Test
    void testUpdate() {

        var borrowingRecords = testDataProvider.getBorrowingRecords();
        assertFalse(borrowingRecords.isEmpty());

        for (var borrowingRecord : borrowingRecords) {
            assertEquals(borrowingRecord, borrowingRecordService.update(borrowingRecord.getId(), borrowingRecord));
            verify(borrowingRecordRepository, times(1)).existsById(borrowingRecord.getId());
            verify(borrowingRecordRepository, times(1)).save(borrowingRecord);
        }

        assertThrows(RuntimeException.class, () -> borrowingRecordService.update(-1L, newBorrowingRecord));
        verify(borrowingRecordRepository, times(1)).existsById(-1L);
        verify(borrowingRecordRepository, never()).save(newBorrowingRecord);
    }

    @Test
    void testDeleteById() {

        var borrowingRecords = testDataProvider.getBorrowingRecords();
        assertFalse(borrowingRecords.isEmpty());

        for (var borrowingRecord : borrowingRecords) {
            borrowingRecordService.deleteById(borrowingRecord.getId());
            verify(borrowingRecordRepository, times(1)).deleteById(borrowingRecord.getId());
        }
    }
}