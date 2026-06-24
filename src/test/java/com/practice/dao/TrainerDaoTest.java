package com.practice.dao;

import com.practice.entity.Trainer;
import com.practice.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerDaoTest {

    @Mock
    private Storage storage;

    @InjectMocks
    private TrainerDao trainerDao;

    private HashMap<Long, Trainer> trainers;

    @BeforeEach
    void setUp() {
        trainers = new HashMap<>();
        when(storage.getTrainers()).thenReturn(trainers);
    }

    @Test
    void create_ShouldGenerateIdOne_WhenStorageIsEmpty() {
        Trainer trainer = new Trainer();

        Trainer created = trainerDao.create(trainer);

        assertEquals(1L, created.getId());
        assertTrue(created.isActive());
        assertEquals(created, trainers.get(1L));
    }

    @Test
    void create_ShouldGenerateNextId_WhenStorageNotEmpty() {
        Trainer existing = new Trainer();
        existing.setId(5L);

        trainers.put(5L, existing);

        Trainer trainer = new Trainer();

        Trainer created = trainerDao.create(trainer);

        assertEquals(6L, created.getId());
        assertEquals(created, trainers.get(6L));
    }

    @Test
    void update_ShouldReplaceTrainer() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);

        trainerDao.update(trainer);

        assertEquals(trainer, trainers.get(1L));
    }

    @Test
    void findById_ShouldReturnTrainer_WhenExists() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);

        trainers.put(1L, trainer);

        Optional<Trainer> result = trainerDao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void findById_ShouldReturnEmptyOptional_WhenTrainerNotExists() {
        Optional<Trainer> result = trainerDao.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllTrainers() {
        Trainer trainer1 = new Trainer();
        trainer1.setId(1L);

        Trainer trainer2 = new Trainer();
        trainer2.setId(2L);

        trainers.put(1L, trainer1);
        trainers.put(2L, trainer2);

        var result = trainerDao.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(trainer1));
        assertTrue(result.contains(trainer2));
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenStorageIsEmpty() {
        var result = trainerDao.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}