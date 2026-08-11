package com.practice.workloadservice.dto;

public record MonthSummaryResponse(
        int month,
        int trainingSummaryDuration
) {
}
