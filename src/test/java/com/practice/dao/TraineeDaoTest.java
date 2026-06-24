package com.practice.dao;

import com.practice.entity.Trainee;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeDaoTest {

    @Mock
    private Storage storage;

    @InjectMocks
    private TraineeDao traineeDao;

    private Map<Long, Trainee> trainees;

    @BeforeEach
    void setUp() {
        trainees = new HashMap<>();
        when(storage.getTrainees()).thenReturn(trainees);
    }

    @Test
    void create_ShouldGenerateIdAndSaveTrainee_WhenStorageIsEmpty() {
        Trainee trainee = new Trainee();

        Trainee created = traineeDao.create(trainee);

        assertEquals(1L, created.getId());
        assertTrue(created.isActive());
        assertEquals(created, trainees.get(1L));
    }

    @Test
    void create_ShouldGenerateNextId_WhenStorageContainsData() {
        Trainee existing = new Trainee();
        existing.setId(5L);

        trainees.put(5L, existing);

        Trainee trainee = new Trainee();

        Trainee created = traineeDao.create(trainee);

        assertEquals(6L, created.getId());
        assertEquals(created, trainees.get(6L));
    }

    @Test
    void update_ShouldReplaceExistingTrainee() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);

        traineeDao.update(trainee);

        assertEquals(trainee, trainees.get(1L));
    }

    @Test
    void delete_ShouldRemoveTrainee() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);

        trainees.put(1L, trainee);

        traineeDao.delete(1L);

        assertFalse(trainees.containsKey(1L));
    }

    @Test
    void findById_ShouldReturnTrainee_WhenExists() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);

        trainees.put(1L, trainee);

        Optional<Trainee> result = traineeDao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void findById_ShouldReturnEmptyOptional_WhenNotExists() {
        Optional<Trainee> result = traineeDao.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllTrainees() {
        Trainee trainee1 = new Trainee();
        trainee1.setId(1L);

        Trainee trainee2 = new Trainee();
        trainee2.setId(2L);

        trainees.put(1L, trainee1);
        trainees.put(2L, trainee2);

        var result = traineeDao.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(trainee1));
        assertTrue(result.contains(trainee2));
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenStorageIsEmpty() {
        var result = traineeDao.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}