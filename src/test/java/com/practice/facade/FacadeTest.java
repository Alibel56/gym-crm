package com.practice.facade;

import com.practice.dao.TrainingTypeRepository;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.TrainingType;
import com.practice.service.TraineeService;
import com.practice.service.TrainerService;
import com.practice.service.TrainingService;
import com.practice.utility.Criteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacadeTest {

    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "password123";
    private static final String NEW_PASSWORD = "newPassword456";

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    private GymFacade gymFacade;

    @BeforeEach
    void setUp() {
        gymFacade = new GymFacade(traineeService, trainerService, trainingService, trainingTypeRepository);
    }

    @Test
    void createTrainee_delegatesToTraineeServiceAndReturnsId() {
        Trainee trainee = Trainee.builder().id(1L).build();
        when(traineeService.create(trainee)).thenReturn(42L);

        long result = gymFacade.createTrainee(trainee);

        assertEquals(42L, result);
        verify(traineeService).create(trainee);
        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void updateTrainee_delegatesToTraineeServiceAndReturnsResult() {
        Trainee trainee = Trainee.builder().id(1L).build();
        when(traineeService.update(USERNAME, PASSWORD, trainee)).thenReturn(true);

        boolean result = gymFacade.updateTrainee(USERNAME, PASSWORD, trainee);

        assertTrue(result);
        verify(traineeService).update(USERNAME, PASSWORD, trainee);
    }

    @Test
    void deleteTrainee_delegatesToTraineeServiceAndReturnsResult() {
        when(traineeService.delete(USERNAME, PASSWORD)).thenReturn(true);

        boolean result = gymFacade.deleteTrainee(USERNAME, PASSWORD);

        assertTrue(result);
        verify(traineeService).delete(USERNAME, PASSWORD);
    }

    @Test
    void findTraineeById_delegatesToTraineeServiceAndReturnsResult() {
        Trainee trainee = Trainee.builder().id(1L).build();
        when(traineeService.findById(1L)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = gymFacade.findTraineeById(1L);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
        verify(traineeService).findById(1L);
    }

    @Test
    void findTraineeById_notFound_returnsEmptyOptional() {
        when(traineeService.findById(1L)).thenReturn(Optional.empty());

        Optional<Trainee> result = gymFacade.findTraineeById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findTraineeByUsername_delegatesToTraineeServiceAndReturnsResult() {
        Trainee trainee = Trainee.builder().id(1L).build();
        when(traineeService.findByUsername(USERNAME, PASSWORD)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = gymFacade.findTraineeByUsername(USERNAME, PASSWORD);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
        verify(traineeService).findByUsername(USERNAME, PASSWORD);
    }

    @Test
    void findAllTrainees_delegatesToTraineeServiceAndReturnsResult() {
        List<Trainee> trainees = List.of(Trainee.builder().id(1L).build(), Trainee.builder().id(2L).build());
        when(traineeService.findAll()).thenReturn(trainees);

        List<Trainee> result = gymFacade.findAllTrainees();

        assertEquals(trainees, result);
        verify(traineeService).findAll();
    }

    @Test
    void switchActiveTrainee_delegatesToTraineeService() {
        gymFacade.switchActiveTrainee(USERNAME, PASSWORD);

        verify(traineeService).switchActive(USERNAME, PASSWORD);
        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void changeTraineePassword_delegatesToTraineeService() {
        gymFacade.changeTraineePassword(USERNAME, PASSWORD, NEW_PASSWORD);

        verify(traineeService).changePassword(USERNAME, PASSWORD, NEW_PASSWORD);
        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void updateTraineeTrainersList_delegatesToTraineeService() {
        List<Long> trainerIds = List.of(1L, 2L, 3L);

        gymFacade.updateTraineeTrainersList(USERNAME, PASSWORD, trainerIds);

        verify(traineeService).updateTrainersList(USERNAME, PASSWORD, trainerIds);
        verifyNoMoreInteractions(traineeService);
    }

    @Test
    void isTraineeMatching_delegatesToTraineeServiceAndReturnsResult() {
        when(traineeService.isMatching(USERNAME, PASSWORD)).thenReturn(true);

        boolean result = gymFacade.isTraineeMatching(USERNAME, PASSWORD);

        assertTrue(result);
        verify(traineeService).isMatching(USERNAME, PASSWORD);
    }

    @Test
    void isTraineeMatching_nonMatching_returnsFalse() {
        when(traineeService.isMatching(USERNAME, "wrongPassword")).thenReturn(false);

        boolean result = gymFacade.isTraineeMatching(USERNAME, "wrongPassword");

        assertFalse(result);
    }

    @Test
    void createTrainer_delegatesToTrainerServiceAndReturnsId() {
        Trainer trainer = Trainer.builder().id(1L).build();
        when(trainerService.create(trainer)).thenReturn(77L);

        long result = gymFacade.createTrainer(trainer);

        assertEquals(77L, result);
        verify(trainerService).create(trainer);
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void updateTrainer_delegatesToTrainerServiceAndReturnsResult() {
        Trainer trainer = Trainer.builder().id(1L).build();
        when(trainerService.update(USERNAME, PASSWORD, trainer)).thenReturn(true);

        boolean result = gymFacade.updateTrainer(USERNAME, PASSWORD, trainer);

        assertTrue(result);
        verify(trainerService).update(USERNAME, PASSWORD, trainer);
    }

    @Test
    void findTrainerById_delegatesToTrainerServiceAndReturnsResult() {
        Trainer trainer = Trainer.builder().id(1L).build();
        when(trainerService.findById(1L)).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = gymFacade.findTrainerById(1L);

        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
        verify(trainerService).findById(1L);
    }

    @Test
    void findAllTrainers_delegatesToTrainerServiceAndReturnsResult() {
        List<Trainer> trainers = List.of(Trainer.builder().id(1L).build(), Trainer.builder().id(2L).build());
        when(trainerService.findAll()).thenReturn(trainers);

        List<Trainer> result = gymFacade.findAllTrainers();

        assertEquals(trainers, result);
        verify(trainerService).findAll();
    }

    @Test
    void findTrainerByUsername_delegatesToTrainerServiceAndReturnsResult() {
        Trainer trainer = Trainer.builder().id(1L).build();
        when(trainerService.findByUsername(USERNAME, PASSWORD)).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = gymFacade.findTrainerByUsername(USERNAME, PASSWORD);

        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
        verify(trainerService).findByUsername(USERNAME, PASSWORD);
    }

    @Test
    void findAllTrainersNotAssignedToTrainee_delegatesToTrainerServiceAndReturnsResult() {
        List<Trainer> trainers = List.of(Trainer.builder().id(1L).build());
        when(trainerService.findAllNotAssignedByTraineeUserName(USERNAME, PASSWORD)).thenReturn(trainers);

        List<Trainer> result = gymFacade.findAllTrainersNotAssignedToTrainee(USERNAME, PASSWORD);

        assertEquals(trainers, result);
        verify(trainerService).findAllNotAssignedByTraineeUserName(USERNAME, PASSWORD);
    }

    @Test
    void switchActiveTrainer_delegatesToTrainerService() {
        gymFacade.switchActiveTrainer(USERNAME, PASSWORD);

        verify(trainerService).switchActive(USERNAME, PASSWORD);
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void changeTrainerPassword_delegatesToTrainerService() {
        gymFacade.changeTrainerPassword(USERNAME, PASSWORD, NEW_PASSWORD);

        verify(trainerService).changePassword(USERNAME, PASSWORD, NEW_PASSWORD);
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    void isTrainerMatching_delegatesToTrainerServiceAndReturnsResult() {
        when(trainerService.isMatching(USERNAME, PASSWORD)).thenReturn(true);

        boolean result = gymFacade.isTrainerMatching(USERNAME, PASSWORD);

        assertTrue(result);
        verify(trainerService).isMatching(USERNAME, PASSWORD);
    }

    @Test
    void createTraining_delegatesToTrainingServiceAndReturnsId() {
        Training training = Training.builder().id(1L).build();
        when(trainingService.create(USERNAME, PASSWORD, training)).thenReturn(500L);

        Long result = gymFacade.createTraining(USERNAME, PASSWORD, training);

        assertEquals(500L, result);
        verify(trainingService).create(USERNAME, PASSWORD, training);
        verifyNoMoreInteractions(trainingService);
    }

    @Test
    void findTrainingById_delegatesToTrainingServiceAndReturnsResult() {
        Training training = Training.builder().id(1L).build();
        when(trainingService.findById(1L)).thenReturn(Optional.of(training));

        Optional<Training> result = gymFacade.findTrainingById(1L);

        assertTrue(result.isPresent());
        assertEquals(training, result.get());
        verify(trainingService).findById(1L);
    }

    @Test
    void findTrainingById_notFound_returnsEmptyOptional() {
        when(trainingService.findById(1L)).thenReturn(Optional.empty());

        Optional<Training> result = gymFacade.findTrainingById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllTrainings_delegatesToTrainingServiceAndReturnsResult() {
        List<Training> trainings = List.of(Training.builder().id(1L).build(), Training.builder().id(2L).build());
        when(trainingService.findAll()).thenReturn(trainings);

        List<Training> result = gymFacade.findAllTrainings();

        assertEquals(trainings, result);
        verify(trainingService).findAll();
    }

    @Test
    void findAllTraineeTrainingsByCriteria_delegatesToTrainingServiceAndReturnsResult() {
        Criteria criteria = mock(Criteria.class);
        List<Training> trainings = List.of(Training.builder().id(1L).build());
        when(trainingService.findAllTraineeTrainingsByCriteria(USERNAME, PASSWORD, criteria))
                .thenReturn(trainings);

        List<Training> result = gymFacade.findAllTraineeTrainingsByCriteria(USERNAME, PASSWORD, criteria);

        assertEquals(trainings, result);
        verify(trainingService).findAllTraineeTrainingsByCriteria(USERNAME, PASSWORD, criteria);
    }

    @Test
    void findAllTrainerTrainingsByCriteria_delegatesToTrainingServiceAndReturnsResult() {
        Criteria criteria = mock(Criteria.class);
        List<Training> trainings = List.of(Training.builder().id(1L).build());
        when(trainingService.findAllTrainerTrainingsByCriteria(USERNAME, PASSWORD, criteria))
                .thenReturn(trainings);

        List<Training> result = gymFacade.findAllTrainerTrainingsByCriteria(USERNAME, PASSWORD, criteria);

        assertEquals(trainings, result);
        verify(trainingService).findAllTrainerTrainingsByCriteria(USERNAME, PASSWORD, criteria);
    }

    @Test
    void findAnyTrainingType_returnsFirstAvailableType() {
        TrainingType cardio = TrainingType.builder().id(1L).name("Cardio").build();
        TrainingType yoga = TrainingType.builder().id(2L).name("Yoga").build();
        when(trainingTypeRepository.findAll()).thenReturn(List.of(cardio, yoga));

        TrainingType result = gymFacade.findAnyTrainingType();

        assertEquals(cardio, result);
        verify(trainingTypeRepository).findAll();
        verifyNoMoreInteractions(trainingTypeRepository);
    }

    @Test
    void findAnyTrainingType_noRowsSeeded_throwsIllegalStateException() {
        when(trainingTypeRepository.findAll()).thenReturn(List.of());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> gymFacade.findAnyTrainingType());

        assertTrue(exception.getMessage().contains("No rows in training_types"));
        verify(trainingTypeRepository).findAll();
    }
}