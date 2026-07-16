package com.practice.dto.request;

import jakarta.validation.constraints.NotBlank;


public record UsernameRequest(
        @NotBlank(message = "Username is required")
        String userName
) {}
