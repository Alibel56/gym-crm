package com.practice.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.TrainingType;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Storage {

    @Getter
    private Map<Long, Trainee> trainees;

    @Getter
    private Map<Long, Trainer> trainers;

    @Getter
    private Map<Long, Training> trainings;

    @Getter
    private Map<Long, TrainingType> trainingTypes;

    @Autowired
    public void setTrainees(@Qualifier("trainees") Map<Long, Trainee> trainees) {
        this.trainees = trainees;
    }

    @Autowired
    public void setTrainers(@Qualifier("trainers") Map<Long, Trainer> trainers) {
        this.trainers = trainers;
    }

    @Autowired
    public void setTrainings(@Qualifier("trainings") Map<Long, Training> trainings) {
        this.trainings = trainings;
    }

    @Autowired
    public void setTrainingTypes(@Qualifier("trainingTypes") Map<Long, TrainingType> trainingTypes) {
        this.trainingTypes = trainingTypes;
    }
}
