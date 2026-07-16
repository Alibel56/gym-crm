package com.practice.mapper.response;

import com.practice.dto.response.RegistrationResponse;
import com.practice.entity.Trainee;
import com.practice.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class TraineeRegistrationResponseMapper implements Mapper<Trainee, RegistrationResponse> {

    @Override
    public RegistrationResponse mapFrom(Trainee object) {
        return RegistrationResponse.builder()
                .userName(object.getUser().getUserName())
                .password(object.getUser().getPassword())
                .build();
    }
}
