package com.practice.service;

import com.practice.dao.TraineeRepository;
import com.practice.dao.TrainerRepository;
import com.practice.dao.TrainingRepository;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.TrainingType;
import com.practice.entity.User;
import com.practice.exception.AuthenticationException;
import com.practice.exception.ValidationException;
import com.practice.utility.Criteria;
import com.practice.utility.Validator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    private static final String TRAINEE_USERNAME = "john.doe";
    private static final String TRAINEE_PASSWORD = "traineePass123";
    private static final String TRAINER_USERNAME = "jane.smith";
    private static final String TRAINER_PASSWORD = "trainerPass123";

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @InjectMocks
    private TrainingService trainingService;

    private MockedStatic<Validator> validatorMock;

    @BeforeEach
    void setUp() {
        validatorMock = mockStatic(Validator.class);
    }

    @AfterEach
    void tearDown() {
        validatorMock.close();
    }

    private User buildUser(Long id, String userName, String password) {
        return User.builder()
                .id(id)
                .firstName("First")
                .lastName("Last")
                .userName(userName)
                .password(password)
                .isActive(true)
                .build();
    }

    private TrainingType buildTrainingType(Long id, String name) {
        return TrainingType.builder().id(id).name(name).build();
    }

    private Trainee buildTrainee(Long id, User user) {
        return Trainee.builder()
                .id(id)
                .dateOfBirth(LocalDate.of(1995, 1, 1))
                .address("Some address")
                .user(user)
                .build();
    }

    private Trainer buildTrainer(Long id, User user, TrainingType trainingType) {
        return Trainer.builder()
                .id(id)
                .user(user)
                .trainingType(trainingType)
                .build();
    }

    private Training buildTraining(Long id, Trainee trainee, Trainer trainer, TrainingType trainingType) {
        return Training.builder()
                .id(id)
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Morning session")
                .trainingType(trainingType)
                .trainingDate(LocalDate.now().plusDays(1))
                .trainingDuration(60)
                .build();
    }

    @Test
    void create_successfulCreation_returnsGeneratedId() {
        TrainingType trainingType = buildTrainingType(10L, "CARDIO");
        User traineeUser = buildUser(1L, TRAINEE_USERNAME, TRAINEE_PASSWORD);
        User trainerUser = buildUser(2L, TRAINER_USERNAME, TRAINER_PASSWORD);
        Trainee trainee = buildTrainee(100L, traineeUser);
        Trainer trainer = buildTrainer(200L, trainerUser, trainingType);
        Training trainingToCreate = buildTraining(null, trainee, trainer, trainingType);

        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(trainer.getId())).thenReturn(Optional.of(trainer));

        Training saved = buildTraining(500L, trainee, trainer, trainingType);
        when(trainingRepository.save(trainingToCreate)).thenReturn(saved);

        Long resultId = trainingService.create(TRAINEE_USERNAME, TRAINEE_PASSWORD, trainingToCreate);

        assertEquals(500L, resultId);
        validatorMock.verify(() -> Validator.validateTrainingForCreate(trainingToCreate));
        verify(trainingRepository).save(trainingToCreate);
    }

    @Test
    void create_wrongTraineePassword_throwsAuthenticationException() {
        User traineeUser = buildUser(1L, TRAINEE_USERNAME, TRAINEE_PASSWORD);
        Trainee trainee = buildTrainee(100L, traineeUser);
        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));

        Training training = buildTraining(null, trainee, null, null);

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> trainingService.create(TRAINEE_USERNAME, "wrongPassword", training));

        assertTrue(ex.getMessage().contains(TRAINEE_USERNAME));
        verifyNoInteractions(trainingRepository);
        verifyNoInteractions(trainerRepository);
        validatorMock.verifyNoInteractions();
    }

    @Test
    void create_traineeNotFoundDuringAuth_throwsAuthenticationException() {
        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.empty());
        Training training = buildTraining(null, null, null, null);

        assertThrows(AuthenticationException.class,
                () -> trainingService.create(TRAINEE_USERNAME, TRAINEE_PASSWORD, training));

        verifyNoInteractions(trainingRepository);
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void create_validatorRejectsTraining_throwsValidationException() {
        User traineeUser = buildUser(1L, TRAINEE_USERNAME, TRAINEE_PASSWORD);
        Trainee trainee = buildTrainee(100L, traineeUser);
        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));

        Training training = buildTraining(null, trainee, null, null);
        validatorMock.when(() -> Validator.validateTrainingForCreate(training))
                .thenThrow(new ValidationException("Invalid training data"));

        assertThrows(ValidationException.class,
                () -> trainingService.create(TRAINEE_USERNAME, TRAINEE_PASSWORD, training));

        verifyNoInteractions(trainerRepository);
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void create_trainerNotFound_throwsEntityNotFoundException() {
        User traineeUser = buildUser(1L, TRAINEE_USERNAME, TRAINEE_PASSWORD);
        Trainee trainee = buildTrainee(100L, traineeUser);
        Trainer trainer = buildTrainer(200L, null, null);
        Training training = buildTraining(null, trainee, trainer, null);

        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(trainer.getId())).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> trainingService.create(TRAINEE_USERNAME, TRAINEE_PASSWORD, training));

        assertEquals("Trainer's id in new training not found", ex.getMessage());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void create_trainerSpecializationMismatch_throwsValidationException() {
        TrainingType trainerType = buildTrainingType(10L, "YOGA");
        TrainingType trainingRequestedType = buildTrainingType(20L, "CARDIO");

        User traineeUser = buildUser(1L, TRAINEE_USERNAME, TRAINEE_PASSWORD);
        Trainee trainee = buildTrainee(100L, traineeUser);
        Trainer trainer = buildTrainer(200L, null, trainerType);
        Training training = buildTraining(null, trainee, trainer, trainingRequestedType);

        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(trainer.getId())).thenReturn(Optional.of(trainer));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> trainingService.create(TRAINEE_USERNAME, TRAINEE_PASSWORD, training));

        assertEquals("Trainer's specialization does not match", ex.getMessage());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void create_traineeNotFoundAfterTrainerCheck_throwsEntityNotFoundException() {
        TrainingType trainingType = buildTrainingType(10L, "CARDIO");
        User traineeUser = buildUser(1L, TRAINEE_USERNAME, TRAINEE_PASSWORD);
        Trainee trainee = buildTrainee(100L, traineeUser);
        Trainer trainer = buildTrainer(200L, null, trainingType);
        Training training = buildTraining(null, trainee, trainer, trainingType);

        // First lookup (auth) succeeds, second (post trainer-check) returns empty
        when(traineeRepository.findByUsername(TRAINEE_USERNAME))
                .thenReturn(Optional.of(trainee))
                .thenReturn(Optional.empty());
        when(trainerRepository.findById(trainer.getId())).thenReturn(Optional.of(trainer));

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> trainingService.create(TRAINEE_USERNAME, TRAINEE_PASSWORD, training));

        assertEquals("Trainee's id in new training not found", ex.getMessage());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void create_trainingTraineeDoesNotMatchAuthenticatedTrainee_throwsValidationException() {
        TrainingType trainingType = buildTrainingType(10L, "CARDIO");
        User traineeUser = buildUser(1L, TRAINEE_USERNAME, TRAINEE_PASSWORD);
        Trainee authenticatedTrainee = buildTrainee(100L, traineeUser);
        Trainee mismatchedTrainee = buildTrainee(999L, traineeUser);
        Trainer trainer = buildTrainer(200L, null, trainingType);
        Training training = buildTraining(null, mismatchedTrainee, trainer, trainingType);

        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(authenticatedTrainee));
        when(trainerRepository.findById(trainer.getId())).thenReturn(Optional.of(trainer));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> trainingService.create(TRAINEE_USERNAME, TRAINEE_PASSWORD, training));

        assertEquals("Training's trainee does not match the authenticated trainee", ex.getMessage());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void findById_existingId_returnsTraining() {
        Training training = buildTraining(5L, null, null, null);
        when(trainingRepository.findById(5L)).thenReturn(Optional.of(training));

        Optional<Training> result = trainingService.findById(5L);

        assertTrue(result.isPresent());
        assertEquals(training, result.get());
    }

    @Test
    void findById_nonExistingId_returnsEmptyOptional() {
        when(trainingRepository.findById(5L)).thenReturn(Optional.empty());

        Optional<Training> result = trainingService.findById(5L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_returnsAllTrainings() {
        List<Training> trainings = List.of(
                buildTraining(1L, null, null, null),
                buildTraining(2L, null, null, null)
        );
        when(trainingRepository.findAll()).thenReturn(trainings);

        List<Training> result = trainingService.findAll();

        assertEquals(2, result.size());
        assertEquals(trainings, result);
    }

    @Test
    void findAll_noTrainings_returnsEmptyList() {
        when(trainingRepository.findAll()).thenReturn(List.of());

        List<Training> result = trainingService.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllTraineeTrainingsByCriteria_authenticatedTrainee_returnsFilteredList() {
        User traineeUser = buildUser(1L, TRAINEE_USERNAME, TRAINEE_PASSWORD);
        Trainee trainee = buildTrainee(100L, traineeUser);
        Criteria criteria = mock(Criteria.class);
        List<Training> expected = List.of(buildTraining(1L, trainee, null, null));

        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainingRepository.findAllTraineeTrainingsByCriteria(TRAINEE_USERNAME, criteria))
                .thenReturn(expected);

        List<Training> result = trainingService.findAllTraineeTrainingsByCriteria(
                TRAINEE_USERNAME, TRAINEE_PASSWORD, criteria);

        assertEquals(expected, result);
    }

    @Test
    void findAllTraineeTrainingsByCriteria_wrongPassword_throwsAuthenticationException() {
        User traineeUser = buildUser(1L, TRAINEE_USERNAME, TRAINEE_PASSWORD);
        Trainee trainee = buildTrainee(100L, traineeUser);
        Criteria criteria = mock(Criteria.class);

        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));

        assertThrows(AuthenticationException.class, () ->
                trainingService.findAllTraineeTrainingsByCriteria(TRAINEE_USERNAME, "wrongPassword", criteria));

        verifyNoInteractions(trainingRepository);
    }

    @Test
    void findAllTraineeTrainingsByCriteria_traineeNotFound_throwsAuthenticationException() {
        Criteria criteria = mock(Criteria.class);
        when(traineeRepository.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () ->
                trainingService.findAllTraineeTrainingsByCriteria(TRAINEE_USERNAME, TRAINEE_PASSWORD, criteria));

        verifyNoInteractions(trainingRepository);
    }

    @Test
    void findAllTrainerTrainingsByCriteria_authenticatedTrainer_returnsFilteredList() {
        User trainerUser = buildUser(2L, TRAINER_USERNAME, TRAINER_PASSWORD);
        Trainer trainer = buildTrainer(200L, trainerUser, null);
        Criteria criteria = mock(Criteria.class);
        List<Training> expected = List.of(buildTraining(1L, null, trainer, null));

        when(trainerRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(trainingRepository.findAllTrainerTrainingsByCriteria(TRAINER_USERNAME, criteria))
                .thenReturn(expected);

        List<Training> result = trainingService.findAllTrainerTrainingsByCriteria(
                TRAINER_USERNAME, TRAINER_PASSWORD, criteria);

        assertEquals(expected, result);
    }

    @Test
    void findAllTrainerTrainingsByCriteria_wrongPassword_throwsAuthenticationException() {
        User trainerUser = buildUser(2L, TRAINER_USERNAME, TRAINER_PASSWORD);
        Trainer trainer = buildTrainer(200L, trainerUser, null);
        Criteria criteria = mock(Criteria.class);

        when(trainerRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));

        assertThrows(AuthenticationException.class, () ->
                trainingService.findAllTrainerTrainingsByCriteria(TRAINER_USERNAME, "wrongPassword", criteria));

        verifyNoInteractions(trainingRepository);
    }

    @Test
    void findAllTrainerTrainingsByCriteria_trainerNotFound_throwsAuthenticationException() {
        Criteria criteria = mock(Criteria.class);
        when(trainerRepository.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () ->
                trainingService.findAllTrainerTrainingsByCriteria(TRAINER_USERNAME, TRAINER_PASSWORD, criteria));

        verifyNoInteractions(trainingRepository);
    }
}