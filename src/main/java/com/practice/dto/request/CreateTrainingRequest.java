package com.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateTrainingRequest(
        @NotBlank(message = "Trainee User name is required")
        String traineeUserName,

        @NotBlank(message = "Trainer User name is required")
        String trainerUserName,

        @NotBlank(message = "Training name is required")
        String trainingName,

        @NotNull(message = "Training date is required")
        LocalDate trainingDate,

        @NotNull(message = "Training duration is required")
        Integer trainingDuration
) {}
