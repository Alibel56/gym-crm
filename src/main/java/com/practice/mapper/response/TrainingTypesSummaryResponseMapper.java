package com.practice.mapper.response;

import com.practice.dto.response.TrainingTypesSummaryResponse;
import com.practice.entity.TrainingType;
import com.practice.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class TrainingTypesSummaryResponseMapper implements Mapper<TrainingType, TrainingTypesSummaryResponse> {

    @Override
    public TrainingTypesSummaryResponse mapFrom(TrainingType object) {
        return TrainingTypesSummaryResponse.builder()
                .trainingTypeId(object.getId())
                .trainingTypeName(object.getName())
                .build();
    }
}
