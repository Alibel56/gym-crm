package com.practice.mapper.request;

import com.practice.dto.request.TraineeRegistrationRequest;
import com.practice.entity.Trainee;
import com.practice.entity.User;
import com.practice.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class TraineeRegistrationRequestMapper implements Mapper<TraineeRegistrationRequest, Trainee> {

    @Override
    public Trainee mapFrom(TraineeRegistrationRequest object) {
        return Trainee.builder()
                .user(User.builder()
                        .firstName(object.firstName())
                        .lastName(object.lastName())
                        .build())
                .dateOfBirth(object.dateOfBirth())
                .address(object.address())
                .build();
    }
}
