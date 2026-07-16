package com.practice.mapper.response;

import com.practice.dto.response.GetTraineeProfileResponse;
import com.practice.entity.Trainee;
import com.practice.mapper.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetTraineeProfileResponseMapper implements Mapper<Trainee, GetTraineeProfileResponse> {

    private TrainerSummaryResponseMapper trainerSummaryResponseMapper;

    @Autowired
    public void setTrainerSummaryResponseMapper(TrainerSummaryResponseMapper trainerSummaryResponseMapper) {
        this.trainerSummaryResponseMapper = trainerSummaryResponseMapper;
    }

    @Override
    public GetTraineeProfileResponse mapFrom(Trainee object) {
        return GetTraineeProfileResponse.builder()
                .firstName(object.getUser().getFirstName())
                .lastName(object.getUser().getLastName())
                .dateOfBirth(object.getDateOfBirth())
                .address(object.getAddress())
                .isActive(object.getUser().getIsActive())
                .trainers(object.getTrainers().stream()
                        .map(trainerSummaryResponseMapper::mapFrom)
                        .toList())
                .build();
    }
}
