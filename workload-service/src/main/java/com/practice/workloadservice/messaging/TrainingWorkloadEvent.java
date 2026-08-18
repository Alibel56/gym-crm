package com.practice.workloadservice.messaging;

import java.time.LocalDate;

public record TrainingWorkloadEvent(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        Boolean isActive,
        LocalDate trainingDate,
        Integer trainingDuration,
        WorkloadActionType actionType
) {
}