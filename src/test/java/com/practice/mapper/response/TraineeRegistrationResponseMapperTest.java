package com.practice.mapper.response;

import com.practice.dto.response.RegistrationResponse;
import com.practice.entity.Trainee;
import com.practice.entity.User;
import com.practice.testtags.PositiveTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TraineeRegistrationResponseMapperTest {

    private TraineeRegistrationResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TraineeRegistrationResponseMapper();
    }

    @Test
    @PositiveTest
    void mapFrom_shouldMapTraineeCredentials() {
        User user = User.builder()
                .userName("alibek.aglanov")
                .password("generated-password")
                .build();

        Trainee trainee = Trainee.builder()
                .user(user)
                .build();

        RegistrationResponse result = mapper.mapFrom(trainee);

        assertThat(result).isNotNull();
        assertThat(result.userName()).isEqualTo("alibek.aglanov");
        assertThat(result.password()).isEqualTo("generated-password");
    }
}
