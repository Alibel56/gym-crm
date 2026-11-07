package com.practice.mapper.response;

import com.practice.dto.response.GetTrainerTrainingsListResponse;
import com.practice.entity.Training;
import com.practice.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class GetTrainerTrainingsListResponseMapper implements Mapper<Training, GetTrainerTrainingsListResponse> {
    @Override
    public GetTrainerTrainingsListResponse mapFrom(Training object) {
        return GetTrainerTrainingsListResponse.builder()
                .id(object.getId())
                .trainingName(object.getTrainingName())
                .trainingDate(object.getTrainingDate())
                .trainingType(object.getTrainingType().getName())
                .trainingDuration(object.getTrainingDuration())
                .traineeName(object.getTrainee().getUser().getFirstName() + " " + object.getTrainee().getUser().getLastName())
                .build();
    }
}
