package com.practice.utility;

import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.User;
import com.practice.exception.ValidationException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Validator {

    public void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }

    public void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " is required");
        }
    }

    public void validateUserForCreate(User user) {
        requireNotNull(user, "user");
        requireNotBlank(user.getFirstName(), "firstName");
        requireNotBlank(user.getLastName(), "lastName");
    }

    public void validateTraineeForCreate(Trainee trainee) {
        requireNotNull(trainee, "trainee");
        validateUserForCreate(trainee.getUser());
    }

    public void validateTrainerForCreate(Trainer trainer) {
        requireNotNull(trainer, "trainer");
        validateUserForCreate(trainer.getUser());
        requireNotNull(trainer.getTrainingType(), "trainingType");
    }

    public void validateTrainingForCreate(Training training) {
        requireNotNull(training, "training");
        requireNotNull(training.getTrainee(), "trainee");
        requireNotNull(training.getTrainer(), "trainer");
        requireNotBlank(training.getTrainingName(), "trainingName");
        requireNotNull(training.getTrainingType(), "trainingType");
        requireNotNull(training.getTrainingDate(), "trainingDate");
        requireNotNull(training.getTrainingDuration(), "trainingDuration");
    }
}
