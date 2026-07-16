package com.practice.mapper.response;

import com.practice.dto.response.GetTrainerProfileResponse;
import com.practice.entity.Trainer;
import com.practice.mapper.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class GetTrainerProfileResponseMapper implements Mapper<Trainer, GetTrainerProfileResponse> {

    private TraineeSummaryResponseMapper traineeSummaryResponseMapper;

    @Autowired
    public void setTraineeSummaryResponseMapper(TraineeSummaryResponseMapper traineeSummaryResponseMapper) {
        this.traineeSummaryResponseMapper = traineeSummaryResponseMapper;
    }

    @Override
    public GetTrainerProfileResponse mapFrom(Trainer object) {
        return GetTrainerProfileResponse.builder()
                .firstName(object.getUser().getFirstName())
                .lastName(object.getUser().getLastName())
                .specializationId(object.getTrainingType().getId())
                .isActive(object.getUser().getIsActive())
                .trainees(object.getTrainees().stream()
                        .map(traineeSummaryResponseMapper::mapFrom)
                        .collect(Collectors.toList()))
                .build();
    }
}
