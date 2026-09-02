package com.practice.mapper.request;

import com.practice.dto.request.GetTrainerTrainingsListRequest;
import com.practice.testtags.EdgeCaseTest;
import com.practice.testtags.PositiveTest;
import com.practice.utility.Criteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class GetTrainerTrainingsListRequestMapperTest {

    private GetTrainerTrainingsListRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GetTrainerTrainingsListRequestMapper();
    }

    @Test
    @PositiveTest
    void mapFrom_shouldMapRequestToCriteria() {
        
        LocalDate periodFrom = LocalDate.of(2026, 6, 1);
        LocalDate periodTo = LocalDate.of(2026, 6, 30);

        GetTrainerTrainingsListRequest request =
                new GetTrainerTrainingsListRequest(
                        "trainer.user",
                        periodFrom,
                        periodTo,
                        "John Trainee"
                );

        
        Criteria result = mapper.mapFrom(request);

        
        assertThat(result).isNotNull();
        assertThat(result.getFromDate()).isEqualTo(periodFrom);
        assertThat(result.getToDate()).isEqualTo(periodTo);
        assertThat(result.getTraineeName()).isEqualTo("John Trainee");

        assertThat(result.getTrainerName()).isNull();
        assertThat(result.getTrainingType()).isNull();
    }

    @Test
    @EdgeCaseTest
    void mapFrom_shouldAllowNullFilters() {
        
        GetTrainerTrainingsListRequest request =
                new GetTrainerTrainingsListRequest(
                        "trainer.user",
                        null,
                        null,
                        null
                );

        
        Criteria result = mapper.mapFrom(request);

        
        assertThat(result).isNotNull();
        assertThat(result.getFromDate()).isNull();
        assertThat(result.getToDate()).isNull();
        assertThat(result.getTraineeName()).isNull();
    }
}
