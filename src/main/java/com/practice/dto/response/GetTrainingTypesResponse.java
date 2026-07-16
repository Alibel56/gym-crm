package com.practice.dto.response;

import java.util.List;

public record GetTrainingTypesResponse(
        List<TrainingTypesSummaryResponse> trainingTypes
) {}
