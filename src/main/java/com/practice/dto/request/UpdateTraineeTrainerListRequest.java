package com.practice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateTraineeTrainerListRequest(
        @NotBlank(message = "Trainee User name is required")
        String traineeUserName,

        @NotEmpty(message = "List of Trainers' User names are required")
        List<@Valid UsernameRequest> trainersList
) {}
