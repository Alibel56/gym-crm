package com.practice.mapper.request;

import com.practice.dto.request.TraineeRegistrationRequest;
import com.practice.entity.Trainee;
import com.practice.testtags.EdgeCaseTest;
import com.practice.testtags.PositiveTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TraineeRegistrationRequestMapperTest {

    private TraineeRegistrationRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TraineeRegistrationRequestMapper();
    }

    @Test
    @PositiveTest
    void mapFrom_shouldMapRequestToTrainee() {
        
        LocalDate dateOfBirth = LocalDate.of(2005, 5, 10);

        TraineeRegistrationRequest request =
                new TraineeRegistrationRequest(
                        "Alibek",
                        "Aglanov",
                        dateOfBirth,
                        "Almaty"
                );

        
        Trainee result = mapper.mapFrom(request);

        
        assertThat(result).isNotNull();
        assertThat(result.getDateOfBirth()).isEqualTo(dateOfBirth);
        assertThat(result.getAddress()).isEqualTo("Almaty");

        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getFirstName()).isEqualTo("Alibek");
        assertThat(result.getUser().getLastName()).isEqualTo("Aglanov");

        assertThat(result.getUser().getUserName()).isNull();
        assertThat(result.getUser().getPassword()).isNull();
        assertThat(result.getUser().getIsActive()).isNull();

        assertThat(result.getTrainers()).isEmpty();
        assertThat(result.getTrainings()).isEmpty();
    }

    @Test
    @EdgeCaseTest
    void mapFrom_shouldAllowOptionalFieldsToBeNull() {
        
        TraineeRegistrationRequest request =
                new TraineeRegistrationRequest(
                        "John",
                        "Doe",
                        null,
                        null
                );

        
        Trainee result = mapper.mapFrom(request);

        
        assertThat(result).isNotNull();
        assertThat(result.getDateOfBirth()).isNull();
        assertThat(result.getAddress()).isNull();

        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getFirstName()).isEqualTo("John");
        assertThat(result.getUser().getLastName()).isEqualTo("Doe");
    }
}
