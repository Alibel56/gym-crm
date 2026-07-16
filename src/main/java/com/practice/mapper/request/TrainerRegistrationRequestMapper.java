package com.practice.mapper.request;

import com.practice.dto.request.TrainerRegistrationRequest;
import com.practice.entity.Trainer;
import com.practice.entity.User;
import com.practice.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class TrainerRegistrationRequestMapper implements Mapper<TrainerRegistrationRequest, Trainer> {

    @Override
    public Trainer mapFrom(TrainerRegistrationRequest object) {
        return Trainer.builder()
                .user(User.builder()
                        .firstName(object.firstName())
                        .lastName(object.lastName())
                        .build())
                .build();
    }
}
