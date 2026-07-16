package com.practice.mapper.response;

import com.practice.dto.response.TraineeSummaryResponse;
import com.practice.entity.Trainee;
import com.practice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TraineeSummaryResponseMapperTest {

    private TraineeSummaryResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TraineeSummaryResponseMapper();
    }

    @Test
    void mapFrom_shouldMapTraineeSummary() {
        User user = User.builder()
                .userName("alibek.aglanov")
                .firstName("Alibek")
                .lastName("Aglanov")
                .build();

        Trainee trainee = Trainee.builder()
                .user(user)
                .build();

        TraineeSummaryResponse result = mapper.mapFrom(trainee);

        assertThat(result).isNotNull();
        assertThat(result.userName()).isEqualTo("alibek.aglanov");
        assertThat(result.firstName()).isEqualTo("Alibek");
        assertThat(result.lastName()).isEqualTo("Aglanov");
    }
}