package com.practice.dto.response;

import lombok.Builder;

@Builder
public record TraineeSummaryResponse(
        String userName,
        String firstName,
        String lastName
) {}
