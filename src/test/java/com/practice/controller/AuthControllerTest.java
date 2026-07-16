package com.practice.controller;

import com.practice.dto.request.LoginRequest;
import com.practice.dto.request.TraineeRegistrationRequest;
import com.practice.dto.request.TrainerRegistrationRequest;
import com.practice.dto.request.UpdateLoginRequest;
import com.practice.dto.response.RegistrationResponse;
import com.practice.service.TraineeService;
import com.practice.service.TrainerService;
import com.practice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController();

        authController.setUserService(userService);
        authController.setTraineeService(traineeService);
        authController.setTrainerService(trainerService);
    }

    @Test
    void registerTrainee_shouldReturnRegistrationResponse() {
        TraineeRegistrationRequest request =
                new TraineeRegistrationRequest(
                        "Alibek",
                        "Aglanov",
                        LocalDate.of(2006, 3, 2),
                        "Almaty"
                );

        RegistrationResponse registrationResponse =
                new RegistrationResponse(
                        "Alibek.Aglanov",
                        "generatedPassword"
                );

        when(traineeService.create(request))
                .thenReturn(registrationResponse);

        ResponseEntity<RegistrationResponse> response =
                authController.registerTrainee(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(registrationResponse);
        assertThat(response.getBody().userName())
                .isEqualTo("Alibek.Aglanov");
        assertThat(response.getBody().password())
                .isEqualTo("generatedPassword");

        verify(traineeService).create(request);
        verifyNoInteractions(userService, trainerService);
    }

    @Test
    void registerTrainer_shouldReturnRegistrationResponse() {
        TrainerRegistrationRequest request =
                new TrainerRegistrationRequest(
                        "John",
                        "Smith",
                        1L
                );

        RegistrationResponse registrationResponse =
                new RegistrationResponse(
                        "John.Smith",
                        "generatedPassword"
                );

        when(trainerService.create(request))
                .thenReturn(registrationResponse);

        ResponseEntity<RegistrationResponse> response =
                authController.registerTrainer(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(registrationResponse);
        assertThat(response.getBody().userName())
                .isEqualTo("John.Smith");
        assertThat(response.getBody().password())
                .isEqualTo("generatedPassword");

        verify(trainerService).create(request);
        verifyNoInteractions(userService, traineeService);
    }

    @Test
    void login_shouldAuthenticateUserAndReturnOk() {
        LoginRequest request =
                new LoginRequest(
                        "Alibek.Aglanov",
                        "password123"
                );

        ResponseEntity<Void> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        verify(userService).authenticate(request);
        verifyNoInteractions(traineeService, trainerService);
    }

    @Test
    void changePassword_shouldChangePasswordAndReturnOk() {
        UpdateLoginRequest request =
                new UpdateLoginRequest(
                        "Alibek.Aglanov",
                        "oldPassword",
                        "newPassword"
                );

        ResponseEntity<Void> response =
                authController.changePassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        verify(userService).changePassword(request);
        verifyNoInteractions(traineeService, trainerService);
    }

    @Test
    void registerTrainee_shouldPropagateServiceException() {
        TraineeRegistrationRequest request =
                new TraineeRegistrationRequest(
                        "Alibek",
                        "Aglanov",
                        LocalDate.of(2006, 3, 2),
                        "Almaty"
                );

        RuntimeException exception =
                new RuntimeException("Unable to register trainee");

        when(traineeService.create(request))
                .thenThrow(exception);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> authController.registerTrainee(request)
                )
                .isSameAs(exception)
                .hasMessage("Unable to register trainee");

        verify(traineeService).create(request);
        verifyNoInteractions(userService, trainerService);
    }

    @Test
    void registerTrainer_shouldPropagateServiceException() {
        TrainerRegistrationRequest request =
                new TrainerRegistrationRequest(
                        "John",
                        "Smith",
                        1L
                );

        RuntimeException exception =
                new RuntimeException("Unable to register trainer");

        when(trainerService.create(request))
                .thenThrow(exception);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> authController.registerTrainer(request)
                )
                .isSameAs(exception)
                .hasMessage("Unable to register trainer");

        verify(trainerService).create(request);
        verifyNoInteractions(userService, traineeService);
    }

    @Test
    void login_shouldPropagateAuthenticationException() {
        LoginRequest request =
                new LoginRequest(
                        "Alibek.Aglanov",
                        "wrongPassword"
                );

        RuntimeException exception =
                new RuntimeException("Invalid username or password");

        doThrow(exception)
                .when(userService)
                .authenticate(request);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> authController.login(request)
                )
                .isSameAs(exception)
                .hasMessage("Invalid username or password");

        verify(userService).authenticate(request);
        verifyNoInteractions(traineeService, trainerService);
    }

    @Test
    void changePassword_shouldPropagateServiceException() {
        UpdateLoginRequest request =
                new UpdateLoginRequest(
                        "Alibek.Aglanov",
                        "wrongOldPassword",
                        "newPassword"
                );

        RuntimeException exception =
                new RuntimeException("Invalid old password");

        doThrow(exception)
                .when(userService)
                .changePassword(request);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> authController.changePassword(request)
                )
                .isSameAs(exception)
                .hasMessage("Invalid old password");

        verify(userService).changePassword(request);
        verifyNoInteractions(traineeService, trainerService);
    }
}