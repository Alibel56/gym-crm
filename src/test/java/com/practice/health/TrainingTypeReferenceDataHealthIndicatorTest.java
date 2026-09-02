package com.practice.health;

import com.practice.dto.response.TrainingTypesSummaryResponse;
import com.practice.service.TrainingTypeService;
import com.practice.testtags.EdgeCaseTest;
import com.practice.testtags.ExceptionTest;
import com.practice.testtags.NegativeTest;
import com.practice.testtags.PositiveTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeReferenceDataHealthIndicatorTest {

    @Mock
    private TrainingTypeService trainingTypeService;

    @InjectMocks
    private TrainingTypeReferenceDataHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator.setTrainingTypeService(trainingTypeService);
    }

    @Test
    @PositiveTest
    void health_shouldReturnUp_whenTrainingTypesExist() {
        // Arrange
        TrainingTypesSummaryResponse firstTrainingType =
                mock(TrainingTypesSummaryResponse.class);

        TrainingTypesSummaryResponse secondTrainingType =
                mock(TrainingTypesSummaryResponse.class);

        when(trainingTypeService.findAll())
                .thenReturn(List.of(firstTrainingType, secondTrainingType));

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("trainingTypesCount", 2);

        verify(trainingTypeService).findAll();
        verifyNoMoreInteractions(trainingTypeService);
    }

    @Test
    @EdgeCaseTest
    void health_shouldReturnDown_whenTrainingTypesAreEmpty() {
        // Arrange
        when(trainingTypeService.findAll())
                .thenReturn(List.of());

        // Act
        Health health = healthIndicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("reason", "No training types configured");

        verify(trainingTypeService).findAll();
        verifyNoMoreInteractions(trainingTypeService);
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void health_shouldReturnDown_whenServiceThrowsException() {
        RuntimeException exception =
                new RuntimeException("Database connection failed");

        when(trainingTypeService.findAll())
                .thenThrow(exception);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry(
                        "error",
                        "java.lang.RuntimeException: Database connection failed"
                );

        verify(trainingTypeService).findAll();
        verifyNoMoreInteractions(trainingTypeService);
    }
}
