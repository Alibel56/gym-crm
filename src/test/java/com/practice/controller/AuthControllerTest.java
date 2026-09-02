package com.practice.controller;

import com.practice.dto.request.LoginRequest;
import com.practice.dto.request.TraineeRegistrationRequest;
import com.practice.dto.request.TrainerRegistrationRequest;
import com.practice.dto.request.UpdateLoginRequest;
import com.practice.dto.response.LoginResponse;
import com.practice.dto.response.RegistrationResponse;
import com.practice.service.TraineeService;
import com.practice.service.TrainerService;
import com.practice.service.UserService;
import com.practice.testtags.PositiveTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
    @PositiveTest
    void registerTraineeShouldReturnRegistrationResponse() {
        TraineeRegistrationRequest request =
                mock(TraineeRegistrationRequest.class);

        RegistrationResponse expectedResponse =
                mock(RegistrationResponse.class);

        when(traineeService.create(request))
                .thenReturn(expectedResponse);

        ResponseEntity<RegistrationResponse> response =
                authController.registerTrainee(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expectedResponse);

        verify(traineeService).create(request);
        verifyNoMoreInteractions(traineeService);
    }

    @Test
    @PositiveTest
    void registerTrainerShouldReturnRegistrationResponse() {
        TrainerRegistrationRequest request =
                mock(TrainerRegistrationRequest.class);

        RegistrationResponse expectedResponse =
                mock(RegistrationResponse.class);

        when(trainerService.create(request))
                .thenReturn(expectedResponse);

        ResponseEntity<RegistrationResponse> response =
                authController.registerTrainer(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expectedResponse);

        verify(trainerService).create(request);
        verifyNoMoreInteractions(trainerService);
    }

    @Test
    @PositiveTest
    void loginShouldReturnLoginResponse() {
        LoginRequest request = mock(LoginRequest.class);
        LoginResponse expectedResponse = mock(LoginResponse.class);

        when(userService.login(request))
                .thenReturn(expectedResponse);

        ResponseEntity<LoginResponse> response =
                authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expectedResponse);

        verify(userService).login(request);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @PositiveTest
    void changePasswordShouldReturnOk() {
        UpdateLoginRequest request =
                mock(UpdateLoginRequest.class);

        ResponseEntity<Void> response =
                authController.changePassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        verify(userService).changePassword(request);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @PositiveTest
    void logoutShouldReturnNoContent() {
        String authorizationHeader = "Bearer test-token";

        ResponseEntity<Void> response =
                authController.logout(authorizationHeader);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(response.getBody()).isNull();

        verify(userService).logout(authorizationHeader);
        verifyNoMoreInteractions(userService);
    }
}
