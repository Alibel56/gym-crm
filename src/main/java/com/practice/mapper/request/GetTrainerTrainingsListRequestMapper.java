package com.practice.mapper.request;

import com.practice.dto.request.GetTrainerTrainingsListRequest;
import com.practice.mapper.Mapper;
import com.practice.utility.Criteria;
import org.springframework.stereotype.Component;

@Component
public class GetTrainerTrainingsListRequestMapper implements Mapper<GetTrainerTrainingsListRequest, Criteria> {
    @Override
    public Criteria mapFrom(GetTrainerTrainingsListRequest object) {
        return Criteria.builder()
                .fromDate(object.periodFrom())
                .toDate(object.periodTo())
                .traineeName(object.traineeName())
                .build();
    }
}
