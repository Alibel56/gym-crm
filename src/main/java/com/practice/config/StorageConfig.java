package com.practice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.TrainingType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class StorageConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    @Bean("trainees")
    public Map<Long, Trainee> trainees() {
        return new ConcurrentHashMap<>();
    }

    @Bean("trainers")
    public Map<Long, Trainer> trainers() {
        return new ConcurrentHashMap<>();
    }

    @Bean("trainings")
    public Map<Long, Training> trainings() {
        return new ConcurrentHashMap<>();
    }

    @Bean("trainingTypes")
    public Map<Long, TrainingType> trainingTypes() {
        return new ConcurrentHashMap<>();
    }
}
