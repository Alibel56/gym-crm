package com.practice.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record GetTrainerProfileResponse(
        String firstName,
        String lastName,
        Long specializationId,
        Boolean isActive,
        List<TraineeSummaryResponse> trainees
) {}
