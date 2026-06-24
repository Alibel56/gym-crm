package com.practice.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.TrainingType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

@Slf4j
@Component
public class DataInitializer {

    private Storage storage;
    private ObjectMapper objectMapper;
    private String fileName;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    @Autowired
    public void setFileName(@Value("${db.file.path}") String fileName) {
        this.fileName = fileName;
    }

    @PostConstruct
    public void init() {
        try {
            File file = new File(fileName);

            if (!file.exists()) {
                log.warn("Database file not found: {}", fileName);
                return;
            }

            JsonNode root = objectMapper.readTree(file);

            if (root.has("trainees")) {
                Map<Long, Trainee> trainees = objectMapper.convertValue(
                        root.get("trainees"),
                        new TypeReference<Map<Long, Trainee>>() {}
                );

                storage.getTrainees().putAll(trainees);
            }

            if (root.has("trainers")) {
                Map<Long, Trainer> trainers = objectMapper.convertValue(
                        root.get("trainers"),
                        new TypeReference<Map<Long, Trainer>>() {}
                );

                storage.getTrainers().putAll(trainers);
            }

            if (root.has("trainings")) {
                Map<Long, Training> trainings = objectMapper.convertValue(
                        root.get("trainings"),
                        new TypeReference<Map<Long, Training>>() {}
                );

                storage.getTrainings().putAll(trainings);
            }

            if (root.has("trainingTypes")) {
                Map<Long, TrainingType> trainingTypes = objectMapper.convertValue(
                        root.get("trainingTypes"),
                        new TypeReference<Map<Long, TrainingType>>() {}
                );

                storage.getTrainingTypes().putAll(trainingTypes);
            }

            log.info(
                    "Database loaded. trainees={}, trainers={}, trainings={}, trainingTypes={}",
                    storage.getTrainees().size(),
                    storage.getTrainers().size(),
                    storage.getTrainings().size(),
                    storage.getTrainingTypes().size()
            );

        } catch (Exception e) {
            log.error("Failed to load database from {}", fileName, e);
            throw new RuntimeException(e);
        }
    }
}
