package com.practice.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/trainees/John.Doe");
    }

    @Test
    void authenticationException_mapsTo401() {
        ResponseEntity<ErrorResponse> response = handler.handleAuthentication(
                new AuthenticationException("Invalid username or password"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().message()).isEqualTo("Invalid username or password");
        assertThat(response.getBody().path()).isEqualTo("/api/trainees/John.Doe");
    }

    @Test
    void entityNotFoundException_mapsTo404() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                new EntityNotFoundException("Trainee John.Doe not found"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).isEqualTo("Trainee John.Doe not found");
    }

    @Test
    void validationException_mapsTo400() {
        ResponseEntity<ErrorResponse> response = handler.handleValidation(
                new ValidationException("firstName is required"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void illegalArgumentException_mapsTo400() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
                new IllegalArgumentException("TrainingType with id 99 does not exist"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void unexpectedException_mapsTo500() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(
                new RuntimeException("boom"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Unexpected error occurred");
    }
}
