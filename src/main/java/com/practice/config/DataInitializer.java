package com.practice.config;

import com.practice.dao.TrainingTypeRepository;
import com.practice.entity.TrainingType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final TrainingTypeRepository trainingTypeRepository;

    @Transactional
    public void init() {

        if (trainingTypeRepository.findAll().isEmpty()) {

            trainingTypeRepository.save(TrainingType.builder().name("Fitness").build());
            trainingTypeRepository.save(TrainingType.builder().name("Yoga").build());
            trainingTypeRepository.save(TrainingType.builder().name("Boxing").build());
            trainingTypeRepository.save(TrainingType.builder().name("Cardio").build());
        }
    }
}