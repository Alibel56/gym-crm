package com.practice.service;

import com.practice.dao.TraineeDao;
import com.practice.dao.TrainerDao;
import com.practice.dao.TrainingDao;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TraineeDao traineeDao;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void create_WhenDataIsValid_ShouldCreateTraining() {
        TrainingType type = new TrainingType();

        Trainer trainer = new Trainer();
        trainer.setSpecialization(type);

        Training training = new Training();
        training.setTrainerId(1L);
        training.setTraineeId(2L);
        training.setTrainingType(type);

        Training saved = new Training();
        saved.setId(100L);

        when(trainerDao.findById(1L))
                .thenReturn(Optional.of(trainer));

        when(traineeDao.findById(2L))
                .thenReturn(Optional.of(new Trainee()));

        when(trainingDao.create(any(Training.class)))
                .thenReturn(saved);

        Long result = trainingService.create(training);

        assertEquals(100L, result);

        verify(trainerDao).findById(1L);
        verify(traineeDao).findById(2L);
        verify(trainingDao).create(training);
    }

    @Test
    void create_WhenTrainerNotFound_ShouldThrowException() {
        Training training = new Training();
        training.setTrainerId(1L);

        when(trainerDao.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> trainingService.create(training)
        );

        verify(trainingDao, never()).create(any());
    }

    @Test
    void create_WhenTrainerSpecializationDoesNotMatch_ShouldThrowException() {
        TrainingType trainerType = new TrainingType();
        trainerType.setTrainingTypeName("Yoga");

        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("Boxing");

        Trainer trainer = new Trainer();
        trainer.setSpecialization(trainerType);

        Training training = new Training();
        training.setTrainerId(1L);
        training.setTrainingType(trainingType);

        when(trainerDao.findById(1L))
                .thenReturn(Optional.of(trainer));

        assertThrows(
                IllegalArgumentException.class,
                () -> trainingService.create(training)
        );

        verify(trainingDao, never()).create(any());
    }

    @Test
    void create_WhenTraineeNotFound_ShouldThrowException() {
        TrainingType type = new TrainingType();

        Trainer trainer = new Trainer();
        trainer.setSpecialization(type);

        Training training = new Training();
        training.setTrainerId(1L);
        training.setTraineeId(2L);
        training.setTrainingType(type);

        when(trainerDao.findById(1L))
                .thenReturn(Optional.of(trainer));

        when(traineeDao.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> trainingService.create(training)
        );

        verify(trainingDao, never()).create(any());
    }

    @Test
    void findById_WhenTrainingExists_ShouldReturnTraining() {
        Training training = new Training();

        when(trainingDao.findById(1L))
                .thenReturn(Optional.of(training));

        Optional<Training> result = trainingService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(training, result.get());
    }

    @Test
    void findById_WhenTrainingNotFound_ShouldReturnEmpty() {
        when(trainingDao.findById(1L))
                .thenReturn(Optional.empty());

        Optional<Training> result = trainingService.findById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_WhenTrainingsExist_ShouldReturnList() {
        List<Training> trainings =
                List.of(new Training(), new Training());

        when(trainingDao.findAll())
                .thenReturn(trainings);

        List<Training> result = trainingService.findAll();

        assertEquals(2, result.size());
        assertEquals(trainings, result);
    }

    @Test
    void findAll_WhenNoTrainingsExist_ShouldReturnEmptyList() {
        when(trainingDao.findAll())
                .thenReturn(List.of());

        List<Training> result = trainingService.findAll();

        assertTrue(result.isEmpty());
    }
}