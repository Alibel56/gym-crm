package com.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateTraineeProfileRequest(
        @NotBlank(message = "User name is required")
        String userName,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        LocalDate dateOfBirth,
        String address,

        @NotNull(message = "Active flag is required")
        Boolean isActive
) {}
