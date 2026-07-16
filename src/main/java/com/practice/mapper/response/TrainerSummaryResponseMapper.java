package com.practice.mapper.response;

import com.practice.dto.response.TrainerSummaryResponse;
import com.practice.entity.Trainer;
import com.practice.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class TrainerSummaryResponseMapper implements Mapper<Trainer, TrainerSummaryResponse> {
    @Override
    public TrainerSummaryResponse mapFrom(Trainer object) {
        return TrainerSummaryResponse.builder()
                .userName(object.getUser().getUserName())
                .firstName(object.getUser().getFirstName())
                .lastName(object.getUser().getLastName())
                .specializationId(object.getTrainingType().getId())
                .build();
    }
}
