package com.practice.runner;

import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.TrainingType;
import com.practice.entity.User;
import com.practice.facade.GymFacade;
import com.practice.utility.Criteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemoRunnerTest {

    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "password123";
    private static final String NEW_PASSWORD = "newPassword456";

    @Mock
    private GymFacade facade;

    private DemoRunner demoRunner;

    @BeforeEach
    void setUp() {
        demoRunner = new DemoRunner(facade);
    }

    @Test
    void pickTrainingType_delegatesToFacadeAndReturnsResult() {
        TrainingType trainingType = TrainingType.builder().id(1L).name("Cardio").build();
        when(facade.findAnyTrainingType()).thenReturn(trainingType);

        TrainingType result = demoRunner.pickTrainingType();

        assertEquals(trainingType, result);
        verify(facade).findAnyTrainingType();
        verifyNoMoreInteractions(facade);
    }

    @Test
    void pickTrainingType_propagatesFacadeException() {
        when(facade.findAnyTrainingType())
                .thenThrow(new IllegalStateException("No rows in training_types"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> demoRunner.pickTrainingType());

        assertTrue(exception.getMessage().contains("No rows in training_types"));
    }

    @Test
    void createTrainee_buildsTraineeAndDelegatesToFacade() {
        when(facade.createTrainee(any(Trainee.class))).thenReturn(42L);

        DemoRunner.CreatedTrainee result = demoRunner.createTrainee();

        assertEquals(42L, result.id());
        assertEquals("John", result.trainee().getUser().getFirstName());
        assertEquals("Smith", result.trainee().getUser().getLastName());
        assertEquals("221B Baker Street", result.trainee().getAddress());
        verify(facade).createTrainee(result.trainee());
        verifyNoMoreInteractions(facade);
    }

    @Test
    void createTrainer_usesProvidedTrainingTypeAndDelegatesToFacade() {
        TrainingType trainingType = TrainingType.builder().id(1L).name("Yoga").build();
        when(facade.createTrainer(any(Trainer.class))).thenReturn(7L);

        DemoRunner.CreatedTrainer result = demoRunner.createTrainer(trainingType);

        assertEquals(7L, result.id());
        assertEquals("Anna", result.trainer().getUser().getFirstName());
        assertEquals(trainingType, result.trainer().getTrainingType());
        verify(facade).createTrainer(result.trainer());
        verifyNoMoreInteractions(facade);
    }

    @Test
    void isTraineeMatching_delegatesToFacadeAndReturnsResult() {
        when(facade.isTraineeMatching(USERNAME, PASSWORD)).thenReturn(true);

        boolean result = demoRunner.isTraineeMatching(USERNAME, PASSWORD);

        assertTrue(result);
        verify(facade).isTraineeMatching(USERNAME, PASSWORD);
    }

    @Test
    void isTraineeMatching_nonMatching_returnsFalse() {
        when(facade.isTraineeMatching(USERNAME, "wrongPassword")).thenReturn(false);

        boolean result = demoRunner.isTraineeMatching(USERNAME, "wrongPassword");

        assertFalse(result);
    }

    @Test
    void isTrainerMatching_delegatesToFacadeAndReturnsResult() {
        when(facade.isTrainerMatching(USERNAME, PASSWORD)).thenReturn(true);

        boolean result = demoRunner.isTrainerMatching(USERNAME, PASSWORD);

        assertTrue(result);
        verify(facade).isTrainerMatching(USERNAME, PASSWORD);
    }

    @Test
    void findTraineeByUsername_delegatesToFacadeAndReturnsResult() {
        Trainee trainee = Trainee.builder().id(1L).build();
        when(facade.findTraineeByUsername(USERNAME, PASSWORD)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = demoRunner.findTraineeByUsername(USERNAME, PASSWORD);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
        verify(facade).findTraineeByUsername(USERNAME, PASSWORD);
    }

    @Test
    void findTrainerByUsername_notFound_returnsEmptyOptional() {
        when(facade.findTrainerByUsername(USERNAME, PASSWORD)).thenReturn(Optional.empty());

        Optional<Trainer> result = demoRunner.findTrainerByUsername(USERNAME, PASSWORD);

        assertTrue(result.isEmpty());
    }

    @Test
    void changeTraineePassword_delegatesToFacade() {
        demoRunner.changeTraineePassword(USERNAME, PASSWORD, NEW_PASSWORD);

        verify(facade).changeTraineePassword(USERNAME, PASSWORD, NEW_PASSWORD);
        verifyNoMoreInteractions(facade);
    }

    @Test
    void changeTrainerPassword_delegatesToFacade() {
        demoRunner.changeTrainerPassword(USERNAME, PASSWORD, NEW_PASSWORD);

        verify(facade).changeTrainerPassword(USERNAME, PASSWORD, NEW_PASSWORD);
        verifyNoMoreInteractions(facade);
    }

    @Test
    void renameTraineeFirstName_updatesUserAndDelegatesToFacade() {
        Trainee trainee = Trainee.builder().user(User.builder().firstName("John").build()).build();

        demoRunner.renameTraineeFirstName(trainee, "Jonathan", USERNAME, PASSWORD);

        assertEquals("Jonathan", trainee.getUser().getFirstName());
        verify(facade).updateTrainee(USERNAME, PASSWORD, trainee);
        verifyNoMoreInteractions(facade);
    }

    @Test
    void renameTrainerFirstName_updatesUserAndDelegatesToFacade() {
        Trainer trainer = Trainer.builder().user(User.builder().firstName("Anna").build()).build();

        demoRunner.renameTrainerFirstName(trainer, "Annabelle", USERNAME, PASSWORD);

        assertEquals("Annabelle", trainer.getUser().getFirstName());
        verify(facade).updateTrainer(USERNAME, PASSWORD, trainer);
        verifyNoMoreInteractions(facade);
    }

    @Test
    void toggleTraineeActive_delegatesToFacade() {
        demoRunner.toggleTraineeActive(USERNAME, PASSWORD);

        verify(facade).switchActiveTrainee(USERNAME, PASSWORD);
        verifyNoMoreInteractions(facade);
    }

    @Test
    void toggleTrainerActive_delegatesToFacade() {
        demoRunner.toggleTrainerActive(USERNAME, PASSWORD);

        verify(facade).switchActiveTrainer(USERNAME, PASSWORD);
        verifyNoMoreInteractions(facade);
    }

    @Test
    void findAllTrainersNotAssignedToTrainee_delegatesToFacadeAndReturnsResult() {
        List<Trainer> trainers = List.of(Trainer.builder().id(1L).build());
        when(facade.findAllTrainersNotAssignedToTrainee(USERNAME, PASSWORD)).thenReturn(trainers);

        List<Trainer> result = demoRunner.findAllTrainersNotAssignedToTrainee(USERNAME, PASSWORD);

        assertEquals(trainers, result);
        verify(facade).findAllTrainersNotAssignedToTrainee(USERNAME, PASSWORD);
    }

    @Test
    void assignTrainerToTrainee_passesSingleTrainerIdAsList() {
        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);

        demoRunner.assignTrainerToTrainee(USERNAME, PASSWORD, 7L);

        verify(facade).updateTraineeTrainersList(eq(USERNAME), eq(PASSWORD), captor.capture());
        assertEquals(List.of(7L), captor.getValue());
        verifyNoMoreInteractions(facade);
    }

    @Test
    void createTraining_buildsTrainingAndDelegatesToFacade() {
        Trainee trainee = Trainee.builder().id(1L).build();
        Trainer trainer = Trainer.builder().id(1L).build();
        TrainingType trainingType = TrainingType.builder().id(1L).name("Cardio").build();
        when(facade.createTraining(eq(USERNAME), eq(PASSWORD), any(Training.class))).thenReturn(500L);

        Long result = demoRunner.createTraining(trainee, trainer, trainingType, USERNAME, PASSWORD);

        assertEquals(500L, result);
        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
        verify(facade).createTraining(eq(USERNAME), eq(PASSWORD), captor.capture());
        Training captured = captor.getValue();
        assertEquals(trainee, captured.getTrainee());
        assertEquals(trainer, captured.getTrainer());
        assertEquals(trainingType, captured.getTrainingType());
        assertEquals("Morning Cardio", captured.getTrainingName());
        assertEquals(60, captured.getTrainingDuration());
    }

    @Test
    void findTraineeTrainings_delegatesToFacadeAndReturnsResult() {
        Training training = Training.builder().id(1L).build();
        when(facade.findAllTraineeTrainingsByCriteria(eq(USERNAME), eq(PASSWORD), any(Criteria.class)))
                .thenReturn(List.of(training));

        List<Training> result = demoRunner.findTraineeTrainings(USERNAME, PASSWORD);

        assertEquals(List.of(training), result);
        verify(facade).findAllTraineeTrainingsByCriteria(eq(USERNAME), eq(PASSWORD), any(Criteria.class));
    }

    @Test
    void findTrainerTrainings_delegatesToFacadeAndReturnsResult() {
        Training training = Training.builder().id(1L).build();
        when(facade.findAllTrainerTrainingsByCriteria(eq(USERNAME), eq(PASSWORD), any(Criteria.class)))
                .thenReturn(List.of(training));

        List<Training> result = demoRunner.findTrainerTrainings(USERNAME, PASSWORD);

        assertEquals(List.of(training), result);
        verify(facade).findAllTrainerTrainingsByCriteria(eq(USERNAME), eq(PASSWORD), any(Criteria.class));
    }

    @Test
    void deleteTrainee_delegatesToFacadeAndReturnsResult() {
        when(facade.deleteTrainee(USERNAME, PASSWORD)).thenReturn(true);

        boolean result = demoRunner.deleteTrainee(USERNAME, PASSWORD);

        assertTrue(result);
        verify(facade).deleteTrainee(USERNAME, PASSWORD);
    }

    @Test
    void deleteTrainee_notFound_returnsFalse() {
        when(facade.deleteTrainee(USERNAME, PASSWORD)).thenReturn(false);

        boolean result = demoRunner.deleteTrainee(USERNAME, PASSWORD);

        assertFalse(result);
    }
}