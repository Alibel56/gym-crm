package com.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SwitchActiveFlagRequest(
        @NotBlank(message = "User name is required")
        String userName,

        @NotNull(message = "Active flag is required")
        Boolean isActive
) {}
