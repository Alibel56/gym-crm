package com.practice.dto.request;

import com.practice.dto.response.TrainingTypesSummaryResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTrainerProfileRequest(
        @NotBlank(message = "User name is required")
        String userName,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

//        Long specializationId

        @NotNull(message = "Active flag is required")
        Boolean isActive
) {}
