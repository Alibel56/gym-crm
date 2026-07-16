package com.practice.controller;

import com.practice.dto.request.*;
import com.practice.dto.response.GetTraineeProfileResponse;
import com.practice.dto.response.GetTraineeTrainingsListResponse;
import com.practice.dto.response.TrainerSummaryResponse;
import com.practice.dto.response.UpdateTraineeProfileResponse;
import com.practice.service.TraineeService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    private TraineeController traineeController;

    @BeforeEach
    void setUp() {
        traineeController = new TraineeController();
        traineeController.setTraineeService(traineeService);
        traineeController.setTrainerService(trainerService);
        traineeController.setTrainingService(trainingService);
    }

    @Test
    void getProfile_shouldReturnTraineeProfile() {
        
        GetTraineeProfileResponse expectedResponse =
                mock(GetTraineeProfileResponse.class);

        when(traineeService.findByUsername(
                new UsernameRequest("trainee.user")
        )).thenReturn(expectedResponse);

        
        ResponseEntity<GetTraineeProfileResponse> response =
                traineeController.getProfile("trainee.user");

        
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isSameAs(expectedResponse);

        verify(traineeService).findByUsername(
                new UsernameRequest("trainee.user")
        );
    }

    @Test
    void updateProfile_shouldReturnUpdatedProfile() {
        
        UpdateTraineeProfileRequest request =
                mock(UpdateTraineeProfileRequest.class);

        UpdateTraineeProfileResponse expectedResponse =
                mock(UpdateTraineeProfileResponse.class);

        when(traineeService.update(request))
                .thenReturn(expectedResponse);

        
        ResponseEntity<UpdateTraineeProfileResponse> response =
                traineeController.updateProfile(request);

        

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isSameAs(expectedResponse);

        verify(traineeService).update(request);
    }

    @Test
    void deleteProfile_shouldDeleteTraineeAndReturnOk() {
        
        ResponseEntity<Void> response =
                traineeController.deleteProfile("trainee.user");

        

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNull();

        verify(traineeService).delete(
                new UsernameRequest("trainee.user")
        );
    }

    @Test
    void switchActive_shouldCallServiceAndReturnOk() {
        
        SwitchActiveFlagRequest request =
                new SwitchActiveFlagRequest(
                        "trainee.user",
                        false
                );

        
        ResponseEntity<Void> response =
                traineeController.switchActive(request);

        

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNull();

        verify(traineeService).switchActive(request);
    }

    @Test
    void getUnassignedTrainers_shouldReturnTrainerSummaries() {
        
        TrainerSummaryResponse firstResponse =
                mock(TrainerSummaryResponse.class);

        TrainerSummaryResponse secondResponse =
                mock(TrainerSummaryResponse.class);

        List<TrainerSummaryResponse> expectedResponses =
                List.of(firstResponse, secondResponse);

        when(trainerService.findAllNotAssignedByTraineeUserName(
                new UsernameRequest("trainee.user")
        )).thenReturn(expectedResponses);

        
        ResponseEntity<List<TrainerSummaryResponse>> response =
                traineeController.getUnassignedTrainers("trainee.user");

        

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .containsExactly(firstResponse, secondResponse);

        verify(trainerService)
                .findAllNotAssignedByTraineeUserName(
                        new UsernameRequest("trainee.user")
                );
    }

    @Test
    void updateTrainersList_shouldReturnUpdatedTrainerList() {
        
        UpdateTraineeTrainerListRequest request =
                mock(UpdateTraineeTrainerListRequest.class);

        TrainerSummaryResponse firstResponse =
                mock(TrainerSummaryResponse.class);

        TrainerSummaryResponse secondResponse =
                mock(TrainerSummaryResponse.class);

        List<TrainerSummaryResponse> expectedResponses =
                List.of(firstResponse, secondResponse);

        when(traineeService.updateTrainersList(request))
                .thenReturn(expectedResponses);

        
        ResponseEntity<List<TrainerSummaryResponse>> response =
                traineeController.updateTrainersList(request);

        

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .containsExactly(firstResponse, secondResponse);

        verify(traineeService).updateTrainersList(request);
    }

    @Test
    void getTrainings_shouldCreateRequestAndReturnTrainings() {
        
        LocalDate periodFrom = LocalDate.of(2026, 1, 1);
        LocalDate periodTo = LocalDate.of(2026, 1, 31);

        GetTraineeTrainingsListResponse firstResponse =
                mock(GetTraineeTrainingsListResponse.class);

        GetTraineeTrainingsListResponse secondResponse =
                mock(GetTraineeTrainingsListResponse.class);

        List<GetTraineeTrainingsListResponse> expectedResponses =
                List.of(firstResponse, secondResponse);

        when(trainingService.findAllTraineeTrainings(
                any(GetTraineeTrainingsListRequest.class)
        )).thenReturn(expectedResponses);

        
        ResponseEntity<List<GetTraineeTrainingsListResponse>> response =
                traineeController.getTrainings(
                        "trainee.user",
                        periodFrom,
                        periodTo,
                        "John Trainer",
                        "Fitness"
                );

        

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .containsExactly(firstResponse, secondResponse);

        ArgumentCaptor<GetTraineeTrainingsListRequest> requestCaptor =
                ArgumentCaptor.forClass(
                        GetTraineeTrainingsListRequest.class
                );

        verify(trainingService)
                .findAllTraineeTrainings(requestCaptor.capture());

        GetTraineeTrainingsListRequest capturedRequest =
                requestCaptor.getValue();

        assertThat(capturedRequest.userName())
                .isEqualTo("trainee.user");

        assertThat(capturedRequest.periodFrom())
                .isEqualTo(periodFrom);

        assertThat(capturedRequest.periodTo())
                .isEqualTo(periodTo);

        assertThat(capturedRequest.trainerName())
                .isEqualTo("John Trainer");

        assertThat(capturedRequest.trainingType())
                .isEqualTo("Fitness");
    }

    @Test
    void getTrainings_shouldSupportNullOptionalFilters() {
        
        when(trainingService.findAllTraineeTrainings(
                any(GetTraineeTrainingsListRequest.class)
        )).thenReturn(List.of());

        
        ResponseEntity<List<GetTraineeTrainingsListResponse>> response =
                traineeController.getTrainings(
                        "trainee.user",
                        null,
                        null,
                        null,
                        null
                );

        
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isEmpty();

        ArgumentCaptor<GetTraineeTrainingsListRequest> requestCaptor =
                ArgumentCaptor.forClass(
                        GetTraineeTrainingsListRequest.class
                );

        verify(trainingService)
                .findAllTraineeTrainings(requestCaptor.capture());

        GetTraineeTrainingsListRequest capturedRequest =
                requestCaptor.getValue();

        assertThat(capturedRequest.userName())
                .isEqualTo("trainee.user");

        assertThat(capturedRequest.periodFrom()).isNull();
        assertThat(capturedRequest.periodTo()).isNull();
        assertThat(capturedRequest.trainerName()).isNull();
        assertThat(capturedRequest.trainingType()).isNull();
    }
}