package com.practice.mapper.response;

import com.practice.dto.response.RegistrationResponse;
import com.practice.entity.Trainer;
import com.practice.entity.User;
import com.practice.testtags.PositiveTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerRegistrationResponseMapperTest {

    private TrainerRegistrationResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TrainerRegistrationResponseMapper();
    }

    @Test
    @PositiveTest
    void mapFrom_shouldMapTrainerCredentials() {
        User user = User.builder()
                .userName("john.smith")
                .password("generated-password")
                .build();

        Trainer trainer = Trainer.builder()
                .user(user)
                .build();

        RegistrationResponse result = mapper.mapFrom(trainer);

        assertThat(result).isNotNull();
        assertThat(result.userName()).isEqualTo("john.smith");
        assertThat(result.password()).isEqualTo("generated-password");
    }
}
