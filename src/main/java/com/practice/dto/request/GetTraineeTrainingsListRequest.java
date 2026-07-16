package com.practice.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record GetTraineeTrainingsListRequest(
        @NotBlank(message = "User name is required")
        String userName,

        LocalDate periodFrom,
        LocalDate periodTo,
        String trainerName,
        String trainingType
) {}
