package com.practice.dao;

import com.practice.entity.TrainingType;
import com.practice.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeDaoTest {

    @Mock
    private Storage storage;

    @InjectMocks
    private TrainingTypeDao trainingTypeDao;

    private Map<Long, TrainingType> trainingTypes;

    @BeforeEach
    void setUp() {
        trainingTypes = new HashMap<>();
        when(storage.getTrainingTypes()).thenReturn(trainingTypes);
    }

    @Test
    void findAll_ShouldReturnAllTrainingTypes() {
        TrainingType type1 = new TrainingType();
        type1.setId(1L);

        TrainingType type2 = new TrainingType();
        type2.setId(2L);

        trainingTypes.put(1L, type1);
        trainingTypes.put(2L, type2);

        var result = trainingTypeDao.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(type1));
        assertTrue(result.contains(type2));
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenStorageIsEmpty() {
        var result = trainingTypeDao.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}