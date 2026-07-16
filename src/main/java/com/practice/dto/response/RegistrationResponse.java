package com.practice.dto.response;

import lombok.Builder;

@Builder
public record RegistrationResponse(
        String userName,
        String password
) {}
