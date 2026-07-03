package com.practice.utility;

import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.TrainingType;
import com.practice.entity.User;
import com.practice.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    private User buildValidUser() {
        return User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("john.doe")
                .password("password123")
                .isActive(true)
                .build();
    }

    private TrainingType buildTrainingType() {
        return TrainingType.builder().id(1L).name("CARDIO").build();
    }

    private Trainee buildValidTrainee() {
        return Trainee.builder()
                .id(1L)
                .dateOfBirth(LocalDate.of(1995, 1, 1))
                .address("Some address")
                .user(buildValidUser())
                .build();
    }

    private Trainer buildValidTrainer() {
        return Trainer.builder()
                .id(1L)
                .user(buildValidUser())
                .trainingType(buildTrainingType())
                .build();
    }

    private Training buildValidTraining() {
        return Training.builder()
                .id(1L)
                .trainee(buildValidTrainee())
                .trainer(buildValidTrainer())
                .trainingName("Morning session")
                .trainingType(buildTrainingType())
                .trainingDate(LocalDate.now().plusDays(1))
                .trainingDuration(60)
                .build();
    }

    @Test
    void requireNotBlank_validValue_doesNotThrow() {
        assertDoesNotThrow(() -> Validator.requireNotBlank("someValue", "fieldName"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void requireNotBlank_blankOrNullValue_throwsValidationException(String value) {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.requireNotBlank(value, "firstName"));

        assertEquals("firstName is required", ex.getMessage());
    }

    @Test
    void requireNotNull_nonNullValue_doesNotThrow() {
        assertDoesNotThrow(() -> Validator.requireNotNull(new Object(), "fieldName"));
    }

    @Test
    void requireNotNull_nullValue_throwsValidationException() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.requireNotNull(null, "trainer"));

        assertEquals("trainer is required", ex.getMessage());
    }

    @Test
    void validateUserForCreate_validUser_doesNotThrow() {
        assertDoesNotThrow(() -> Validator.validateUserForCreate(buildValidUser()));
    }

    @Test
    void validateUserForCreate_nullUser_throwsValidationException() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateUserForCreate(null));

        assertEquals("user is required", ex.getMessage());
    }

    @Test
    void validateUserForCreate_blankFirstName_throwsValidationException() {
        User user = buildValidUser();
        user.setFirstName("  ");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateUserForCreate(user));

        assertEquals("firstName is required", ex.getMessage());
    }

    @Test
    void validateUserForCreate_nullFirstName_throwsValidationException() {
        User user = buildValidUser();
        user.setFirstName(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateUserForCreate(user));

        assertEquals("firstName is required", ex.getMessage());
    }

    @Test
    void validateUserForCreate_blankLastName_throwsValidationException() {
        User user = buildValidUser();
        user.setLastName("");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateUserForCreate(user));

        assertEquals("lastName is required", ex.getMessage());
    }

    @Test
    void validateTraineeForCreate_validTrainee_doesNotThrow() {
        assertDoesNotThrow(() -> Validator.validateTraineeForCreate(buildValidTrainee()));
    }

    @Test
    void validateTraineeForCreate_nullTrainee_throwsValidationException() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTraineeForCreate(null));

        assertEquals("trainee is required", ex.getMessage());
    }

    @Test
    void validateTraineeForCreate_nullUser_throwsValidationException() {
        Trainee trainee = buildValidTrainee();
        trainee.setUser(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTraineeForCreate(trainee));

        assertEquals("user is required", ex.getMessage());
    }

    @Test
    void validateTraineeForCreate_invalidUser_propagatesUserValidationError() {
        Trainee trainee = buildValidTrainee();
        trainee.getUser().setFirstName(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTraineeForCreate(trainee));

        assertEquals("firstName is required", ex.getMessage());
    }

    @Test
    void validateTrainerForCreate_validTrainer_doesNotThrow() {
        assertDoesNotThrow(() -> Validator.validateTrainerForCreate(buildValidTrainer()));
    }

    @Test
    void validateTrainerForCreate_nullTrainer_throwsValidationException() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTrainerForCreate(null));

        assertEquals("trainer is required", ex.getMessage());
    }

    @Test
    void validateTrainerForCreate_invalidUser_propagatesUserValidationError() {
        Trainer trainer = buildValidTrainer();
        trainer.getUser().setLastName(" ");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTrainerForCreate(trainer));

        assertEquals("lastName is required", ex.getMessage());
    }

    @Test
    void validateTrainerForCreate_nullTrainingType_throwsValidationException() {
        Trainer trainer = buildValidTrainer();
        trainer.setTrainingType(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTrainerForCreate(trainer));

        assertEquals("trainingType is required", ex.getMessage());
    }

    @Test
    void validateTrainingForCreate_validTraining_doesNotThrow() {
        assertDoesNotThrow(() -> Validator.validateTrainingForCreate(buildValidTraining()));
    }

    @Test
    void validateTrainingForCreate_nullTraining_throwsValidationException() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTrainingForCreate(null));

        assertEquals("training is required", ex.getMessage());
    }

    @Test
    void validateTrainingForCreate_nullTrainee_throwsValidationException() {
        Training training = buildValidTraining();
        training.setTrainee(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTrainingForCreate(training));

        assertEquals("trainee is required", ex.getMessage());
    }

    @Test
    void validateTrainingForCreate_nullTrainer_throwsValidationException() {
        Training training = buildValidTraining();
        training.setTrainer(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTrainingForCreate(training));

        assertEquals("trainer is required", ex.getMessage());
    }

    @Test
    void validateTrainingForCreate_blankTrainingName_throwsValidationException() {
        Training training = buildValidTraining();
        training.setTrainingName("  ");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTrainingForCreate(training));

        assertEquals("trainingName is required", ex.getMessage());
    }

    @Test
    void validateTrainingForCreate_nullTrainingType_throwsValidationException() {
        Training training = buildValidTraining();
        training.setTrainingType(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTrainingForCreate(training));

        assertEquals("trainingType is required", ex.getMessage());
    }

    @Test
    void validateTrainingForCreate_nullTrainingDate_throwsValidationException() {
        Training training = buildValidTraining();
        training.setTrainingDate(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTrainingForCreate(training));

        assertEquals("trainingDate is required", ex.getMessage());
    }

    @Test
    void validateTrainingForCreate_nullTrainingDuration_throwsValidationException() {
        Training training = buildValidTraining();
        training.setTrainingDuration(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> Validator.validateTrainingForCreate(training));

        assertEquals("trainingDuration is required", ex.getMessage());
    }
}