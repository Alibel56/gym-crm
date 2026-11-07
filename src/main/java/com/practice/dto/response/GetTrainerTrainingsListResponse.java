package com.practice.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record GetTrainerTrainingsListResponse(
        Long id,
        String trainingName,
        LocalDate trainingDate,
        String trainingType,
        Integer trainingDuration,
        String traineeName
) {}
