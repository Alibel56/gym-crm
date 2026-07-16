package com.practice.controller;

import com.practice.dto.request.GetTrainerTrainingsListRequest;
import com.practice.dto.request.SwitchActiveFlagRequest;
import com.practice.dto.request.TrainerRegistrationRequest;
import com.practice.dto.request.UpdateTrainerProfileRequest;
import com.practice.dto.request.UsernameRequest;
import com.practice.dto.response.GetTrainerProfileResponse;
import com.practice.dto.response.GetTrainerTrainingsListResponse;
import com.practice.dto.response.RegistrationResponse;
import com.practice.dto.response.UpdateTrainerProfileResponse;
import com.practice.service.TrainerService;
import com.practice.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    private TrainerController trainerController;

    @BeforeEach
    void setUp() {
        trainerController = new TrainerController();
        trainerController.setTrainerService(trainerService);
        trainerController.setTrainingService(trainingService);
    }

    @Test
    void getProfile_shouldReturnTrainerProfile() {
        
        GetTrainerProfileResponse expectedResponse =
                mock(GetTrainerProfileResponse.class);

        when(trainerService.findByUsername(
                new UsernameRequest("trainer.user")
        )).thenReturn(expectedResponse);

        
        ResponseEntity<GetTrainerProfileResponse> response =
                trainerController.getProfile("trainer.user");

        
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isSameAs(expectedResponse);

        verify(trainerService).findByUsername(
                new UsernameRequest("trainer.user")
        );

        verifyNoInteractions(trainingService);
    }

    @Test
    void updateProfile_shouldReturnUpdatedTrainerProfile() {
        
        UpdateTrainerProfileRequest request =
                mock(UpdateTrainerProfileRequest.class);

        UpdateTrainerProfileResponse expectedResponse =
                mock(UpdateTrainerProfileResponse.class);

        when(trainerService.update(request))
                .thenReturn(expectedResponse);

        
        ResponseEntity<UpdateTrainerProfileResponse> response =
                trainerController.updateProfile(request);

        
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isSameAs(expectedResponse);

        verify(trainerService).update(request);
        verifyNoInteractions(trainingService);
    }

    @Test
    void switchActive_shouldCallServiceAndReturnOk() {
        
        SwitchActiveFlagRequest request =
                new SwitchActiveFlagRequest(
                        "trainer.user",
                        false
                );

        
        ResponseEntity<Void> response =
                trainerController.switchActive(request);

        
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNull();

        verify(trainerService).switchActive(request);
        verifyNoInteractions(trainingService);
    }

    @Test
    void getTrainings_shouldCreateRequestAndReturnTrainings() {
        
        LocalDate periodFrom = LocalDate.of(2026, 1, 1);
        LocalDate periodTo = LocalDate.of(2026, 1, 31);

        GetTrainerTrainingsListResponse firstResponse =
                mock(GetTrainerTrainingsListResponse.class);

        GetTrainerTrainingsListResponse secondResponse =
                mock(GetTrainerTrainingsListResponse.class);

        List<GetTrainerTrainingsListResponse> expectedResponses =
                List.of(firstResponse, secondResponse);

        when(trainingService.findAllTrainerTrainings(
                any(GetTrainerTrainingsListRequest.class)
        )).thenReturn(expectedResponses);

        
        ResponseEntity<List<GetTrainerTrainingsListResponse>> response =
                trainerController.getTrainings(
                        "trainer.user",
                        periodFrom,
                        periodTo,
                        "Trainee Name"
                );

        
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .containsExactly(firstResponse, secondResponse);

        ArgumentCaptor<GetTrainerTrainingsListRequest> requestCaptor =
                ArgumentCaptor.forClass(
                        GetTrainerTrainingsListRequest.class
                );

        verify(trainingService)
                .findAllTrainerTrainings(requestCaptor.capture());

        GetTrainerTrainingsListRequest capturedRequest =
                requestCaptor.getValue();

        assertThat(capturedRequest.userName())
                .isEqualTo("trainer.user");

        assertThat(capturedRequest.periodFrom())
                .isEqualTo(periodFrom);

        assertThat(capturedRequest.periodTo())
                .isEqualTo(periodTo);

        assertThat(capturedRequest.traineeName())
                .isEqualTo("Trainee Name");

        verifyNoMoreInteractions(trainingService);
        verifyNoInteractions(trainerService);
    }

    @Test
    void getTrainings_shouldSupportNullOptionalFilters() {
        
        when(trainingService.findAllTrainerTrainings(
                any(GetTrainerTrainingsListRequest.class)
        )).thenReturn(List.of());

        
        ResponseEntity<List<GetTrainerTrainingsListResponse>> response =
                trainerController.getTrainings(
                        "trainer.user",
                        null,
                        null,
                        null
                );

        
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isEmpty();

        ArgumentCaptor<GetTrainerTrainingsListRequest> requestCaptor =
                ArgumentCaptor.forClass(
                        GetTrainerTrainingsListRequest.class
                );

        verify(trainingService)
                .findAllTrainerTrainings(requestCaptor.capture());

        GetTrainerTrainingsListRequest capturedRequest =
                requestCaptor.getValue();

        assertThat(capturedRequest.userName())
                .isEqualTo("trainer.user");

        assertThat(capturedRequest.periodFrom()).isNull();
        assertThat(capturedRequest.periodTo()).isNull();
        assertThat(capturedRequest.traineeName()).isNull();

        verifyNoInteractions(trainerService);
    }

    @Test
    void getTrainings_shouldReturnEmptyListWhenServiceReturnsEmptyList() {
        
        when(trainingService.findAllTrainerTrainings(
                any(GetTrainerTrainingsListRequest.class)
        )).thenReturn(List.of());

        
        ResponseEntity<List<GetTrainerTrainingsListResponse>> response =
                trainerController.getTrainings(
                        "trainer.user",
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 2, 28),
                        "Unknown Trainee"
                );

        
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull()
                .isEmpty();

        verify(trainingService)
                .findAllTrainerTrainings(
                        any(GetTrainerTrainingsListRequest.class)
                );
    }
}