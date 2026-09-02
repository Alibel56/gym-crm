package com.practice.mapper.request;

import com.practice.dto.request.TrainerRegistrationRequest;
import com.practice.entity.Trainer;
import com.practice.testtags.PositiveTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerRegistrationRequestMapperTest {

    private TrainerRegistrationRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TrainerRegistrationRequestMapper();
    }

    @Test
    @PositiveTest
    void mapFrom_shouldMapRequestToTrainer() {
        
        TrainerRegistrationRequest request =
                new TrainerRegistrationRequest(
                        "John",
                        "Smith",
                        1L
                );

        
        Trainer result = mapper.mapFrom(request);

        
        assertThat(result).isNotNull();

        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getFirstName()).isEqualTo("John");
        assertThat(result.getUser().getLastName()).isEqualTo("Smith");

        assertThat(result.getUser().getUserName()).isNull();
        assertThat(result.getUser().getPassword()).isNull();
        assertThat(result.getUser().getIsActive()).isNull();

        assertThat(result.getTrainingType()).isNull();

        assertThat(result.getTrainees()).isEmpty();
        assertThat(result.getTrainings()).isEmpty();
    }
}
