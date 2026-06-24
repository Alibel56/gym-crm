package com.practice.dao;

import com.practice.entity.Training;
import com.practice.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingDaoTest {

    @Mock
    private Storage storage;

    @InjectMocks
    private TrainingDao trainingDao;

    private Map<Long, Training> trainings;

    @BeforeEach
    void setUp() {
        trainings = new HashMap<>();
        when(storage.getTrainings()).thenReturn(trainings);
    }

    @Test
    void create_ShouldGenerateIdOne_WhenStorageIsEmpty() {
        Training training = new Training();

        Training created = trainingDao.create(training);

        assertEquals(1L, created.getId());
        assertEquals(created, trainings.get(1L));
    }

    @Test
    void create_ShouldGenerateNextId_WhenStorageNotEmpty() {
        Training existing = new Training();
        existing.setId(10L);

        trainings.put(10L, existing);

        Training training = new Training();

        Training created = trainingDao.create(training);

        assertEquals(11L, created.getId());
        assertEquals(created, trainings.get(11L));
    }

    @Test
    void findById_ShouldReturnTraining_WhenExists() {
        Training training = new Training();
        training.setId(1L);

        trainings.put(1L, training);

        Optional<Training> result = trainingDao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(training, result.get());
    }

    @Test
    void findById_ShouldReturnEmptyOptional_WhenNotExists() {
        Optional<Training> result = trainingDao.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllTrainings() {
        Training training1 = new Training();
        training1.setId(1L);

        Training training2 = new Training();
        training2.setId(2L);

        trainings.put(1L, training1);
        trainings.put(2L, training2);

        var result = trainingDao.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(training1));
        assertTrue(result.contains(training2));
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenStorageIsEmpty() {
        var result = trainingDao.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}