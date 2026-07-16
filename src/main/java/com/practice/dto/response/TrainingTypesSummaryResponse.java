package com.practice.dto.response;

import lombok.Builder;

@Builder
public record TrainingTypesSummaryResponse (
        String trainingTypeName,
        Long trainingTypeId
){}
