package com.practice.mapper.response;

import com.practice.dto.response.GetTraineeTrainingsListResponse;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.TrainingType;
import com.practice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class GetTraineeTrainingsListResponseMapperTest {

    private GetTraineeTrainingsListResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GetTraineeTrainingsListResponseMapper();
    }

    @Test
    void mapFrom_shouldMapTrainingToTraineeTrainingResponse() {
        User trainerUser = User.builder()
                .firstName("John")
                .lastName("Smith")
                .build();

        Trainer trainer = Trainer.builder()
                .user(trainerUser)
                .build();

        TrainingType trainingType = TrainingType.builder()
                .id(1L)
                .name("Fitness")
                .build();

        LocalDate trainingDate = LocalDate.of(2026, 7, 16);

        Training training = Training.builder()
                .trainingName("Morning Training")
                .trainingDate(trainingDate)
                .trainingDuration(60)
                .trainingType(trainingType)
                .trainer(trainer)
                .build();

        GetTraineeTrainingsListResponse result =
                mapper.mapFrom(training);

        assertThat(result).isNotNull();
        assertThat(result.trainingName()).isEqualTo("Morning Training");
        assertThat(result.trainingDate()).isEqualTo(trainingDate);
        assertThat(result.trainingType()).isEqualTo("Fitness");
        assertThat(result.trainingDuration()).isEqualTo(60);
        assertThat(result.trainerName()).isEqualTo("John Smith");
    }
}