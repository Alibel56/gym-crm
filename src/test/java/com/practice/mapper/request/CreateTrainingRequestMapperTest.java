package com.practice.mapper.request;

import com.practice.dto.request.CreateTrainingRequest;
import com.practice.entity.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CreateTrainingRequestMapperTest {

    private CreateTrainingRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CreateTrainingRequestMapper();
    }

    @Test
    void mapFrom_shouldMapRequestToTraining() {
        
        CreateTrainingRequest request = new CreateTrainingRequest(
                "trainer.user",
                "trainee.user",
                "Morning Cardio",
                LocalDate.of(2026, 7, 16),
                60
        );

        
        Training result = mapper.mapFrom(request);

        
        assertThat(result).isNotNull();
        assertThat(result.getTrainingName()).isEqualTo("Morning Cardio");
        assertThat(result.getTrainingDate())
                .isEqualTo(LocalDate.of(2026, 7, 16));
        assertThat(result.getTrainingDuration()).isEqualTo(60);

        assertThat(result.getTrainer()).isNull();
        assertThat(result.getTrainee()).isNull();
        assertThat(result.getTrainingType()).isNull();
    }
}