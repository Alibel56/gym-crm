package com.practice.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record UpdateTraineeProfileResponse(
        String userName,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        Boolean isActive,
        List<TrainerSummaryResponse> trainers
) {}
