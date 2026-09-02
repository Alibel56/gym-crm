package com.practice.controller;

import com.practice.dto.request.CreateTrainingRequest;
import com.practice.service.TrainingService;
import com.practice.testtags.PositiveTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    private TrainingController trainingController;

    @BeforeEach
    void setUp() {
        trainingController = new TrainingController();
        trainingController.setTrainingService(trainingService);
    }

    @Test
    @PositiveTest
    void addTraining_shouldCreateTrainingAndReturnOk() {

        CreateTrainingRequest request =
                org.mockito.Mockito.mock(CreateTrainingRequest.class);

        ResponseEntity<Void> response =
                trainingController.addTraining(request);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNull();

        verify(trainingService).create(request);
        verifyNoMoreInteractions(trainingService);
    }
}
