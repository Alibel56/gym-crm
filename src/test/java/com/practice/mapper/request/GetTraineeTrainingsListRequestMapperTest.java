package com.practice.mapper.request;

import com.practice.dto.request.GetTraineeTrainingsListRequest;
import com.practice.testtags.EdgeCaseTest;
import com.practice.testtags.PositiveTest;
import com.practice.utility.Criteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class GetTraineeTrainingsListRequestMapperTest {

    private GetTraineeTrainingsListRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GetTraineeTrainingsListRequestMapper();
    }

    @Test
    @PositiveTest
    void mapFrom_shouldMapRequestToCriteria() {
        
        LocalDate periodFrom = LocalDate.of(2026, 7, 1);
        LocalDate periodTo = LocalDate.of(2026, 7, 31);

        GetTraineeTrainingsListRequest request =
                new GetTraineeTrainingsListRequest(
                        "trainee.user",
                        periodFrom,
                        periodTo,
                        "John Trainer",
                        "Fitness"
                );

        
        Criteria result = mapper.mapFrom(request);

        
        assertThat(result).isNotNull();
        assertThat(result.getFromDate()).isEqualTo(periodFrom);
        assertThat(result.getToDate()).isEqualTo(periodTo);
        assertThat(result.getTrainerName()).isEqualTo("John Trainer");
        assertThat(result.getTrainingType()).isEqualTo("Fitness");
        assertThat(result.getTraineeName()).isNull();
    }

    @Test
    @EdgeCaseTest
    void mapFrom_shouldAllowNullFilters() {
        
        GetTraineeTrainingsListRequest request =
                new GetTraineeTrainingsListRequest(
                        "trainee.user",
                        null,
                        null,
                        null,
                        null
                );

        
        Criteria result = mapper.mapFrom(request);

        
        assertThat(result).isNotNull();
        assertThat(result.getFromDate()).isNull();
        assertThat(result.getToDate()).isNull();
        assertThat(result.getTrainerName()).isNull();
        assertThat(result.getTrainingType()).isNull();
    }
}
