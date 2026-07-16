package com.practice.config;

import com.practice.dao.TrainingTypeRepository;
import com.practice.entity.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer {

    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    public void setTrainingTypeRepository(
            TrainingTypeRepository trainingTypeRepository
    ) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void init() {
        if (!trainingTypeRepository.findAll().isEmpty()) {
            return;
        }

        trainingTypeRepository.save(
                TrainingType.builder()
                        .name("Fitness")
                        .build()
        );

        trainingTypeRepository.save(
                TrainingType.builder()
                        .name("Yoga")
                        .build()
        );

        trainingTypeRepository.save(
                TrainingType.builder()
                        .name("Zumba")
                        .build()
        );

        trainingTypeRepository.save(
                TrainingType.builder()
                        .name("Stretching")
                        .build()
        );

        trainingTypeRepository.save(
                TrainingType.builder()
                        .name("Resistance")
                        .build()
        );
    }
}