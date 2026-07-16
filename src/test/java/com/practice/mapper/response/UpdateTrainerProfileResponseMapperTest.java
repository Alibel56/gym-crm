package com.practice.mapper.response;

import com.practice.dto.response.TraineeSummaryResponse;
import com.practice.dto.response.UpdateTrainerProfileResponse;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.TrainingType;
import com.practice.entity.User;
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
class UpdateTrainerProfileResponseMapperTest {

    @Mock
    private TraineeSummaryResponseMapper traineeSummaryResponseMapper;

    private UpdateTrainerProfileResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UpdateTrainerProfileResponseMapper();
        mapper.setTraineeSummaryResponseMapper(traineeSummaryResponseMapper);
    }

    @Test
    void mapFrom_shouldMapUpdatedTrainerProfile() {
        User user = User.builder()
                .userName("john.smith")
                .firstName("John")
                .lastName("Smith")
                .isActive(true)
                .build();

        TrainingType trainingType = TrainingType.builder()
                .id(3L)
                .name("Boxing")
                .build();

        Trainee firstTrainee = Trainee.builder()
                .id(1L)
                .build();

        Trainee secondTrainee = Trainee.builder()
                .id(2L)
                .build();

        Trainer trainer = Trainer.builder()
                .user(user)
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

        UpdateTrainerProfileResponse result =
                mapper.mapFrom(trainer);

        assertThat(result).isNotNull();
        assertThat(result.userName()).isEqualTo("john.smith");
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Smith");
        assertThat(result.specializationId()).isEqualTo(3L);
        assertThat(result.isActive()).isTrue();
        assertThat(result.trainees())
                .containsExactly(firstSummary, secondSummary);

        verify(traineeSummaryResponseMapper).mapFrom(firstTrainee);
        verify(traineeSummaryResponseMapper).mapFrom(secondTrainee);
    }
}