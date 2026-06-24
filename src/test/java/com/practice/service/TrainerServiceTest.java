package com.practice.service;

import com.practice.dao.TraineeDao;
import com.practice.dao.TrainerDao;
import com.practice.dao.TrainingTypeDao;
import com.practice.entity.Trainer;
import com.practice.entity.TrainingType;
import com.practice.utility.ServiceUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void create_WhenTrainingTypeExists_ShouldCreateTrainer() {
        TrainingType type = new TrainingType();
        Trainer trainer = new Trainer();

        trainer.setFirstName("John");
        trainer.setLastName("Doe");
        trainer.setSpecialization(type);

        Trainer saved = new Trainer();
        saved.setId(1L);

        when(trainingTypeDao.findAll())
                .thenReturn(List.of(type));

        when(trainerDao.findAll())
                .thenReturn(List.of());

        when(trainerDao.create(any(Trainer.class)))
                .thenReturn(saved);

        when(traineeDao.findAll())
                .thenReturn(List.of());

        try (MockedStatic<ServiceUtility> utility =
                     Mockito.mockStatic(ServiceUtility.class)) {

            utility.when(() ->
                            ServiceUtility.generateUserName(anyString(), anyString(), anyList()))
                    .thenReturn("john.doe");

            utility.when(ServiceUtility::generatePassword)
                    .thenReturn("password");

            Long result = trainerService.create(trainer);

            assertEquals(1L, result);
            assertEquals("john.doe", trainer.getUserName());
            assertEquals("password", trainer.getPassword());

            verify(trainerDao).create(trainer);
        }
    }

    @Test
    void create_WhenTrainingTypeDoesNotExist_ShouldThrowException() {
        TrainingType existingType = new TrainingType();
        existingType.setTrainingTypeName("Yoga");

        TrainingType trainerType = new TrainingType();
        trainerType.setTrainingTypeName("Boxing");

        Trainer trainer = new Trainer();
        trainer.setSpecialization(trainerType);

        when(trainingTypeDao.findAll())
                .thenReturn(List.of(existingType));

        assertThrows(
                IllegalArgumentException.class,
                () -> trainerService.create(trainer)
        );

        verify(trainerDao, never()).create(any());
    }

    @Test
    void update_WhenTrainerExists_ShouldReturnTrue() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);

        when(trainerDao.findById(1L))
                .thenReturn(Optional.of(new Trainer()));

        boolean result = trainerService.update(trainer);

        assertTrue(result);
        verify(trainerDao).update(trainer);
    }

    @Test
    void update_WhenTrainerDoesNotExist_ShouldReturnFalse() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);

        when(trainerDao.findById(1L))
                .thenReturn(Optional.empty());

        boolean result = trainerService.update(trainer);

        assertFalse(result);
        verify(trainerDao, never()).update(any());
    }

    @Test
    void findById_WhenTrainerExists_ShouldReturnTrainer() {
        Trainer trainer = new Trainer();

        when(trainerDao.findById(1L))
                .thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void findById_WhenTrainerDoesNotExist_ShouldReturnEmptyOptional() {
        when(trainerDao.findById(1L))
                .thenReturn(Optional.empty());

        Optional<Trainer> result = trainerService.findById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_WhenTrainersExist_ShouldReturnList() {
        List<Trainer> trainers =
                List.of(new Trainer(), new Trainer());

        when(trainerDao.findAll())
                .thenReturn(trainers);

        List<Trainer> result = trainerService.findAll();

        assertEquals(2, result.size());
        assertEquals(trainers, result);
    }

    @Test
    void findAll_WhenNoTrainersExist_ShouldReturnEmptyList() {
        when(trainerDao.findAll())
                .thenReturn(List.of());

        List<Trainer> result = trainerService.findAll();

        assertTrue(result.isEmpty());
    }
}
