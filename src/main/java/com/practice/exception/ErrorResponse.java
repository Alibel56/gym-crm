package com.practice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Standard error payload returned for any failed REST call")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ErrorResponse(

        @Schema(description = "Timestamp when the error occurred")
        LocalDateTime timestamp,

        @Schema(description = "HTTP status code", example = "404")
        int status,

        @Schema(description = "Short error reason phrase", example = "Not Found")
        String error,

        @Schema(description = "Human readable error message")
        String message,

        @Schema(description = "Request path that produced the error")
        String path,

        @Schema(description = "Transaction id correlating this call with the logs")
        String transactionId,

        @Schema(description = "Field level validation errors, if any")
        List<String> validationErrors
) {}