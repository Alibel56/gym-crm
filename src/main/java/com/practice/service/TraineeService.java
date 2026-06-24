package com.practice.service;

import com.practice.dao.TraineeDao;
import com.practice.dao.TrainerDao;
import com.practice.entity.Trainee;
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
public class TraineeService {

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
        log.debug("TraineeService - TraineeDao injected");
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
        log.debug("TraineeService - TrainerDao injected");
    }

    public Long create(Trainee traineeCreate) {
        log.debug("TraineeService - Creating trainee: {} {}",
                traineeCreate.getFirstName(),
                traineeCreate.getLastName());

        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(traineeDao.findAll());
        allUsers.addAll(trainerDao.findAll());

        String userName = ServiceUtility.generateUserName(
                traineeCreate.getFirstName(),
                traineeCreate.getLastName(),
                allUsers);
        String password = ServiceUtility.generatePassword();

        traineeCreate.setUserName(userName);
        traineeCreate.setPassword(password);

        Long id = traineeDao.create(traineeCreate).getId();

        log.info("TraineeService - Trainee created successfully with username {}", userName);

        return id;
    }

    public boolean update(Trainee traineeUpdate){
        log.debug("TraineeService - Updating trainee with id {}", traineeUpdate.getId());

        var maybeEntity = traineeDao.findById(traineeUpdate.getId());
        maybeEntity.ifPresentOrElse(
                t -> traineeDao.update(traineeUpdate),
                () -> log.warn("TraineeService - Failed to update, Trainee with id {} not found", traineeUpdate.getId())
        );
        return maybeEntity.isPresent();
    }

    public boolean delete(Long id){
        log.debug("TraineeService - Deleting trainee with id {}", id);

        var maybeEntity = traineeDao.findById(id);
        maybeEntity.ifPresentOrElse(
                t -> traineeDao.delete(id),
                () -> log.warn("TraineeService - Failed to delete, Trainee with id {} not found", id)
        );
        return maybeEntity.isPresent();
    }

    public Optional<Trainee> findById(Long id){
        log.debug("TraineeService - Searching trainee with id {}", id);

        Optional<Trainee> trainee = traineeDao.findById(id);
        trainee.ifPresentOrElse(
                t -> log.debug("TraineeService - Trainee found with id {}", id),
                () -> log.warn("TraineeService - Trainee with id {} not found", id)
        );

        return trainee;
    }

    public List<Trainee> findAll(){
        log.debug("TraineeService - Retrieving all trainees");

        List<Trainee> list = traineeDao.findAll();
        if (list.isEmpty()){
            log.debug("TraineeService - No trainees found");
        }

        return list;
    }
}
