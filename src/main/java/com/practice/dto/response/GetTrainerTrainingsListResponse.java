package com.practice.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record GetTrainerTrainingsListResponse(
        String trainingName,
        LocalDate trainingDate,
        String trainingType,
        Integer trainingDuration,
        String traineeName
) {}
