package com.practice.mapper.request;

import com.practice.dto.request.GetTraineeTrainingsListRequest;
import com.practice.mapper.Mapper;
import com.practice.utility.Criteria;
import org.springframework.stereotype.Component;

@Component
public class GetTraineeTrainingsListRequestMapper implements Mapper<GetTraineeTrainingsListRequest, Criteria> {
    @Override
    public Criteria mapFrom(GetTraineeTrainingsListRequest object) {
        return Criteria.builder()
                .fromDate(object.periodFrom())
                .toDate(object.periodTo())
                .trainerName(object.trainerName())
                .trainingType(object.trainingType())
                .build();
    }
}
