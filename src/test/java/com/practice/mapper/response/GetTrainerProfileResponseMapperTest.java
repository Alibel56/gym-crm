package com.practice.mapper.response;

import com.practice.dto.response.GetTrainerProfileResponse;
import com.practice.dto.response.TraineeSummaryResponse;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.TrainingType;
import com.practice.entity.User;
import com.practice.testtags.PositiveTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTrainerProfileResponseMapperTest {

    @Mock
    private TraineeSummaryResponseMapper traineeSummaryResponseMapper;

    private GetTrainerProfileResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GetTrainerProfileResponseMapper();
        mapper.setTraineeSummaryResponseMapper(traineeSummaryResponseMapper);
    }

    @Test
    @PositiveTest
    void mapFrom_shouldMapTrainerToResponse() {
        User trainerUser = User.builder()
                .firstName("John")
                .lastName("Smith")
                .isActive(true)
                .build();

        TrainingType trainingType = TrainingType.builder()
                .id(5L)
                .name("Yoga")
                .build();

        Trainee firstTrainee = Trainee.builder()
                .id(1L)
                .build();

        Trainee secondTrainee = Trainee.builder()
                .id(2L)
                .build();

        Trainer trainer = Trainer.builder()
                .user(trainerUser)
                .trainingType(trainingType)
                .trainees(List.of(firstTrainee, secondTrainee))
                .build();

        TraineeSummaryResponse firstSummary =
                org.mockito.Mockito.mock(TraineeSummaryResponse.class);

        TraineeSummaryResponse secondSummary =
                org.mockito.Mockito.mock(TraineeSummaryResponse.class);

        when(traineeSummaryResponseMapper.mapFrom(firstTrainee))
                .thenReturn(firstSummary);

        when(traineeSummaryResponseMapper.mapFrom(secondTrainee))
                .thenReturn(secondSummary);

        GetTrainerProfileResponse result = mapper.mapFrom(trainer);

        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Smith");
        assertThat(result.specializationId()).isEqualTo(5L);
        assertThat(result.isActive()).isTrue();
        assertThat(result.trainees())
                .containsExactly(firstSummary, secondSummary);

        verify(traineeSummaryResponseMapper).mapFrom(firstTrainee);
        verify(traineeSummaryResponseMapper).mapFrom(secondTrainee);
    }
}
