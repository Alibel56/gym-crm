package com.practice.workloadservice.dto;

public record MonthlyWorkloadResponse(
        String trainerUsername,
        int year,
        int month,
        int trainingSummaryDuration
) {
}
