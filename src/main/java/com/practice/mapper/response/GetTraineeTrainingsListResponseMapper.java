package com.practice.mapper.response;

import com.practice.dto.response.GetTraineeTrainingsListResponse;
import com.practice.entity.Training;
import com.practice.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class GetTraineeTrainingsListResponseMapper implements Mapper<Training, GetTraineeTrainingsListResponse> {
    @Override
    public GetTraineeTrainingsListResponse mapFrom(Training object) {
        return GetTraineeTrainingsListResponse.builder()
                .id(object.getId())
                .trainingName(object.getTrainingName())
                .trainingDate(object.getTrainingDate())
                .trainingType(object.getTrainingType().getName())
                .trainingDuration(object.getTrainingDuration())
                .trainerName(object.getTrainer().getUser().getFirstName() + " " + object.getTrainer().getUser().getLastName())
                .build();
    }
}
