package com.practice.mapper.request;

import com.practice.dto.request.CreateTrainingRequest;
import com.practice.entity.Training;
import com.practice.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class CreateTrainingRequestMapper implements Mapper<CreateTrainingRequest, Training> {

    @Override
    public Training mapFrom(CreateTrainingRequest object) {
        return Training.builder()
                .trainingName(object.trainingName())
                .trainingDate(object.trainingDate())
                .trainingDuration(object.trainingDuration())
                .build();
    }
}
