package com.practice.mapper.response;

import com.practice.dto.response.TraineeSummaryResponse;
import com.practice.entity.Trainee;
import com.practice.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class TraineeSummaryResponseMapper implements Mapper<Trainee, TraineeSummaryResponse> {

    @Override
    public TraineeSummaryResponse mapFrom(Trainee object) {
        return TraineeSummaryResponse.builder()
                .userName(object.getUser().getUserName())
                .firstName(object.getUser().getFirstName())
                .lastName(object.getUser().getLastName())
                .build();
    }
}
