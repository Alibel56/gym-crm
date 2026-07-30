package com.practice.dto.response;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds
) {}
