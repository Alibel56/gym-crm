package com.practice.metrics;

import com.practice.dao.TraineeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GymMetricsQueryService {

    private final TraineeRepository traineeRepository;

    public GymMetricsQueryService(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Transactional(readOnly = true)
    public double countActiveTrainees() {
        return traineeRepository.findAll().stream()
                .filter(trainee ->
                        trainee.getUser() != null
                                && Boolean.TRUE.equals(
                                trainee.getUser().getIsActive()
                        )
                )
                .count();
    }
}