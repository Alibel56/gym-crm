package com.practice.mapper.response;

import com.practice.dto.response.GetTraineeProfileResponse;
import com.practice.dto.response.TrainerSummaryResponse;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTraineeProfileResponseMapperTest {

    @Mock
    private TrainerSummaryResponseMapper trainerSummaryResponseMapper;

    private GetTraineeProfileResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GetTraineeProfileResponseMapper();
        mapper.setTrainerSummaryResponseMapper(trainerSummaryResponseMapper);
    }

    @Test
    void mapFrom_shouldMapTraineeToResponse() {
        LocalDate dateOfBirth = LocalDate.of(2005, 5, 10);

        User traineeUser = User.builder()
                .firstName("Alibek")
                .lastName("Aglanov")
                .isActive(true)
                .build();

        Trainer firstTrainer = Trainer.builder()
                .id(1L)
                .build();

        Trainer secondTrainer = Trainer.builder()
                .id(2L)
                .build();

        Trainee trainee = Trainee.builder()
                .user(traineeUser)
                .dateOfBirth(dateOfBirth)
                .address("Almaty")
                .trainers(List.of(firstTrainer, secondTrainer))
                .build();

        TrainerSummaryResponse firstSummary =
                org.mockito.Mockito.mock(TrainerSummaryResponse.class);

        TrainerSummaryResponse secondSummary =
                org.mockito.Mockito.mock(TrainerSummaryResponse.class);

        when(trainerSummaryResponseMapper.mapFrom(firstTrainer))
                .thenReturn(firstSummary);

        when(trainerSummaryResponseMapper.mapFrom(secondTrainer))
                .thenReturn(secondSummary);

        GetTraineeProfileResponse result = mapper.mapFrom(trainee);

        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo("Alibek");
        assertThat(result.lastName()).isEqualTo("Aglanov");
        assertThat(result.dateOfBirth()).isEqualTo(dateOfBirth);
        assertThat(result.address()).isEqualTo("Almaty");
        assertThat(result.isActive()).isTrue();
        assertThat(result.trainers())
                .containsExactly(firstSummary, secondSummary);

        verify(trainerSummaryResponseMapper).mapFrom(firstTrainer);
        verify(trainerSummaryResponseMapper).mapFrom(secondTrainer);
    }
}