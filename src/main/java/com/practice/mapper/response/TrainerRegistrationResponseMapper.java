package com.practice.mapper.response;

import com.practice.dto.response.RegistrationResponse;
import com.practice.entity.Trainer;
import com.practice.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class TrainerRegistrationResponseMapper implements Mapper<Trainer, RegistrationResponse> {

    @Override
    public RegistrationResponse mapFrom(Trainer object) {
        return RegistrationResponse.builder()
                .userName(object.getUser().getUserName())
                .password(object.getUser().getPassword())
                .build();
    }
}
