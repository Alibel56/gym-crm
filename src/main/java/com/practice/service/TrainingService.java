package com.practice.service;

import com.practice.dao.TraineeDao;
import com.practice.dao.TrainerDao;
import com.practice.dao.TrainingDao;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.TrainingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TrainingService {

    private TrainingDao trainingDao;
    private TrainerDao trainerDao;
    private TraineeDao traineeDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
        log.debug("TrainingService - TrainingDao injected");
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
        log.debug("TrainingService - TrainerDao injected");
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
        log.debug("TrainingService - TraineeDao injected");
    }

    public Long create(Training trainingCreate){
        Optional<Trainer> trainer = trainerDao.findById(trainingCreate.getTrainerId());
        if(trainer.isPresent()){
            if (!trainer.get().getSpecialization().equals(trainingCreate.getTrainingType())){
                log.error("TrainingService - Trainer's specialization does not match");
                throw new IllegalArgumentException("Trainer's specialization does not match");
            }
        }else{
            log.error("TrainingService - Trainer with {} id in new training not found",  trainingCreate.getTrainerId());
            throw new IllegalArgumentException("Trainer's id in new training not found");
        }

        if (traineeDao.findById(trainingCreate.getTraineeId()).isEmpty()) {
            log.error("TrainingService - Trainee's id in new training not found");
            throw new IllegalArgumentException("Trainee's id in new training not found");
        }

        return trainingDao.create(trainingCreate).getId();
    }

    public Optional<Training> findById(Long id){
        log.debug("TrainingService - Searching training with id {}", id);

        Optional<Training> training = trainingDao.findById(id);
        training.ifPresentOrElse(
                t -> log.debug("TrainingService - Training found with id {}", id),
                () -> log.warn("TrainingService - Training with id {} not found", id)
        );

        return training;
    }

    public List<Training> findAll(){
        log.debug("TrainingService - Retrieving all trainings");

        List<Training> list = trainingDao.findAll();
        if (list.isEmpty()){
            log.debug("TrainingService - No trainings found");
        }

        return list;
    }
}
