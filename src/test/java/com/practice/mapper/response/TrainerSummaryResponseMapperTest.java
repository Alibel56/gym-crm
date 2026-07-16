package com.practice.mapper.response;

import com.practice.dto.response.TrainerSummaryResponse;
import com.practice.entity.Trainer;
import com.practice.entity.TrainingType;
import com.practice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerSummaryResponseMapperTest {

    private TrainerSummaryResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TrainerSummaryResponseMapper();
    }

    @Test
    void mapFrom_shouldMapTrainerSummary() {
        User user = User.builder()
                .userName("john.smith")
                .firstName("John")
                .lastName("Smith")
                .build();

        TrainingType trainingType = TrainingType.builder()
                .id(4L)
                .name("Cardio")
                .build();

        Trainer trainer = Trainer.builder()
                .user(user)
                .trainingType(trainingType)
                .build();

        TrainerSummaryResponse result = mapper.mapFrom(trainer);

        assertThat(result).isNotNull();
        assertThat(result.userName()).isEqualTo("john.smith");
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Smith");
        assertThat(result.specializationId()).isEqualTo(4L);
    }
}