package com.practice.mapper.response;

import com.practice.dto.response.TrainingTypesSummaryResponse;
import com.practice.entity.TrainingType;
import com.practice.testtags.PositiveTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrainingTypesSummaryResponseMapperTest {

    private TrainingTypesSummaryResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TrainingTypesSummaryResponseMapper();
    }

    @Test
    @PositiveTest
    void mapFrom_shouldMapTrainingTypeSummary() {
        TrainingType trainingType = TrainingType.builder()
                .id(1L)
                .name("Fitness")
                .build();

        TrainingTypesSummaryResponse result =
                mapper.mapFrom(trainingType);

        assertThat(result).isNotNull();
        assertThat(result.trainingTypeId()).isEqualTo(1L);
        assertThat(result.trainingTypeName()).isEqualTo("Fitness");
    }
}
