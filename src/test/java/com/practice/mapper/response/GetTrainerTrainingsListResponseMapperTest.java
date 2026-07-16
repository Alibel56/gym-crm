package com.practice.mapper.response;

import com.practice.dto.response.GetTrainerTrainingsListResponse;
import com.practice.entity.Trainee;
import com.practice.entity.Training;
import com.practice.entity.TrainingType;
import com.practice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class GetTrainerTrainingsListResponseMapperTest {

    private GetTrainerTrainingsListResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GetTrainerTrainingsListResponseMapper();
    }

    @Test
    void mapFrom_shouldMapTrainingToTrainerTrainingResponse() {
        User traineeUser = User.builder()
                .firstName("Alibek")
                .lastName("Aglanov")
                .build();

        Trainee trainee = Trainee.builder()
                .user(traineeUser)
                .build();

        TrainingType trainingType = TrainingType.builder()
                .id(1L)
                .name("Boxing")
                .build();

        LocalDate trainingDate = LocalDate.of(2026, 7, 16);

        Training training = Training.builder()
                .trainingName("Evening Boxing")
                .trainingDate(trainingDate)
                .trainingDuration(90)
                .trainingType(trainingType)
                .trainee(trainee)
                .build();

        GetTrainerTrainingsListResponse result =
                mapper.mapFrom(training);

        assertThat(result).isNotNull();
        assertThat(result.trainingName()).isEqualTo("Evening Boxing");
        assertThat(result.trainingDate()).isEqualTo(trainingDate);
        assertThat(result.trainingType()).isEqualTo("Boxing");
        assertThat(result.trainingDuration()).isEqualTo(90);
        assertThat(result.traineeName()).isEqualTo("Alibek Aglanov");
    }
}