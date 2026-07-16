package com.practice.controller;

import com.practice.dto.response.GetTrainingTypesResponse;
import com.practice.dto.response.TrainingTypesSummaryResponse;
import com.practice.service.TrainingTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeControllerTest {

    @Mock
    private TrainingTypeService trainingTypeService;

    private TrainingTypeController trainingTypeController;

    @BeforeEach
    void setUp() {
        trainingTypeController = new TrainingTypeController();
        trainingTypeController.setTrainingTypeService(trainingTypeService);
    }

    @Test
    void getTrainingTypes_shouldReturnTrainingTypes() {

        TrainingTypesSummaryResponse first =
                mock(TrainingTypesSummaryResponse.class);

        TrainingTypesSummaryResponse second =
                mock(TrainingTypesSummaryResponse.class);

        List<TrainingTypesSummaryResponse> trainingTypes =
                List.of(first, second);

        when(trainingTypeService.findAll())
                .thenReturn(trainingTypes);


        ResponseEntity<GetTrainingTypesResponse> response =
                trainingTypeController.getTrainingTypes();


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().trainingTypes())
                .containsExactly(first, second);

        verify(trainingTypeService).findAll();
        verifyNoMoreInteractions(trainingTypeService);
    }
}