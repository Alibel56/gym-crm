package com.practice.dto.response;

import com.practice.entity.TrainingType;
import lombok.Builder;

@Builder
public record TrainerSummaryResponse(
        String userName,
        String firstName,
        String lastName,
        Long specializationId
) {}
