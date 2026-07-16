package com.practice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "User name is required")
        String userName,

        @NotBlank(message = "Password is required")
        String password
) {}
