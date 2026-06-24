package com.practice.service;

import com.practice.dao.TraineeDao;
import com.practice.dao.TrainerDao;
import com.practice.dao.TrainingTypeDao;
import com.practice.entity.Trainer;
import com.practice.entity.User;
import com.practice.utility.ServiceUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TrainerService {

    private TrainerDao trainerDao;
    private TraineeDao traineeDao;
    private TrainingTypeDao trainingTypeDao;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
        log.debug("TrainerService - TrainerDao injected");
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
        log.debug("TrainerService - TraineeDao injected");
    }

    @Autowired
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
        log.debug("TrainerService - TrainingTypeDao injected");
    }

    public Long create(Trainer trainerCreate){
        log.debug("TrainerService - Creating trainer: {} {}",
                trainerCreate.getFirstName(),
                trainerCreate.getLastName());

        boolean exists = trainingTypeDao.findAll().stream()
                .anyMatch(t -> t.equals(trainerCreate.getSpecialization()));

        if (!exists) {
            log.error("TrainerService - TrainingType does not exist");
            throw new IllegalArgumentException("TrainingType does not exist");
        }

        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(traineeDao.findAll());
        allUsers.addAll(trainerDao.findAll());

        String userName = ServiceUtility.generateUserName(
                trainerCreate.getFirstName(),
                trainerCreate.getLastName(),
                allUsers);
        String password = ServiceUtility.generatePassword();

        trainerCreate.setUserName(userName);
        trainerCreate.setPassword(password);

        Long id =  trainerDao.create(trainerCreate).getId();

        log.info("TrainerService - Trainer created successfully with username {}", userName);

        return id;
    }

    public boolean update(Trainer trainerUpdate){
        log.debug("TrainerService - Updating trainer with id {}", trainerUpdate.getId());

        var maybeEntity = trainerDao.findById(trainerUpdate.getId());
        maybeEntity.ifPresentOrElse(
                t -> trainerDao.update(trainerUpdate),
                () -> log.warn("TrainerService - Failed to update, Trainer with id {} not found", trainerUpdate.getId())
        );
        return maybeEntity.isPresent();
    }

    public Optional<Trainer> findById(Long id){
        log.debug("TrainerService - Searching trainer with id {}", id);

        Optional<Trainer> trainer = trainerDao.findById(id);
        trainer.ifPresentOrElse(
                t -> log.debug("TrainerService - Trainer found with id {}", id),
                () -> log.warn("TrainerService - Trainer with id {} not found", id)
        );

        return trainer;
    }

    public List<Trainer> findAll(){
        log.debug("TrainerService - Retrieving all trainers");

        List<Trainer> list = trainerDao.findAll();
        if (list.isEmpty()){
            log.debug("TrainerService - No trainer found");
        }

        return list;
    }
}
