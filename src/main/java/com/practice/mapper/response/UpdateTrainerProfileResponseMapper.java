package com.practice.mapper.response;

import com.practice.dto.response.UpdateTrainerProfileResponse;
import com.practice.entity.Trainer;
import com.practice.mapper.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateTrainerProfileResponseMapper implements Mapper<Trainer, UpdateTrainerProfileResponse> {

    private TraineeSummaryResponseMapper traineeSummaryResponseMapper;

    @Autowired
    public void setTraineeSummaryResponseMapper(TraineeSummaryResponseMapper traineeSummaryResponseMapper) {
        this.traineeSummaryResponseMapper = traineeSummaryResponseMapper;
    }

    @Override
    public UpdateTrainerProfileResponse mapFrom(Trainer object) {
        return UpdateTrainerProfileResponse.builder()
                .userName(object.getUser().getUserName())
                .firstName(object.getUser().getFirstName())
                .lastName(object.getUser().getLastName())
                .specializationId(object.getTrainingType().getId())
                .isActive(object.getUser().getIsActive())
                .trainees(object.getTrainees().stream()
                        .map(traineeSummaryResponseMapper::mapFrom)
                        .toList())
                .build();
    }
}
