package com.practice.service;


import com.practice.dao.TraineeRepository;
import com.practice.dao.TrainerRepository;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.User;
import com.practice.exception.AuthenticationException;
import com.practice.utility.ServiceUtility;
import com.practice.utility.Validator;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Transactional
    public long create(Trainee traineeCreate) {
        Validator.validateTraineeForCreate(traineeCreate);

        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(traineeRepository.findAll().stream().map(Trainee::getUser).toList());
        allUsers.addAll(trainerRepository.findAll().stream().map(Trainer::getUser).toList());

        String userName = ServiceUtility.generateUserName(
                traineeCreate.getUser().getFirstName(),
                traineeCreate.getUser().getLastName(),
                allUsers);
        String password = ServiceUtility.generatePassword();

        traineeCreate.getUser().setUserName(userName);
        traineeCreate.getUser().setPassword(password);
        traineeCreate.getUser().setIsActive(true);

        Long id = traineeRepository.save(traineeCreate).getId();
        log.info("Created trainee profile id={} username={}", id, userName);
        return id;
    }

    @Transactional
    public boolean update(String authUsername, String authPassword, Trainee traineeUpdate) {
        authenticate(authUsername, authPassword);
        Validator.validateTraineeForCreate(traineeUpdate);

        var maybeEntity = traineeRepository.findById(traineeUpdate.getId());
        maybeEntity.ifPresentOrElse(
                t -> traineeRepository.update(traineeUpdate),
                () -> {throw new EntityNotFoundException("Trainee with id " + traineeUpdate.getId() + " not found");}
        );
        log.info("Updated trainee id={}", traineeUpdate.getId());
        return maybeEntity.isPresent();
    }

    @Transactional
    public boolean delete(String authUsername, String authPassword) {
        authenticate(authUsername, authPassword);
        boolean deleted = traineeRepository.delete(authUsername);
        log.info("Deleted trainee username={} result={}", authUsername, deleted);
        return deleted;
    }

    public Optional<Trainee> findById(Long id) {
        return traineeRepository.findById(id);
    }

    public Optional<Trainee> findByUsername(String authUsername, String authPassword) {
        authenticate(authUsername, authPassword);
        return traineeRepository.findByUsername(authUsername);
    }

    public List<Trainee> findAll() {
        return traineeRepository.findAll();
    }

    @Transactional
    public void switchActive(String authUsername, String authPassword) {
        authenticate(authUsername, authPassword);
        Trainee trainee = traineeRepository.findByUsername(authUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee " + authUsername + " not found"));
        traineeRepository.switchActive(trainee.getId());
        log.info("Toggled isActive for trainee username={}", authUsername);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        authenticate(username, oldPassword);
        Validator.requireNotBlank(newPassword, "newPassword");
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee " + username + " not found"));
        trainee.getUser().setPassword(newPassword);
        traineeRepository.update(trainee);
        log.info("Password changed for trainee username={}", username);
    }

    @Transactional
    public void updateTrainersList(String authUsername, String authPassword, List<Long> trainerIds) {
        authenticate(authUsername, authPassword);
        Trainee trainee = traineeRepository.findByUsername(authUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee " + authUsername + " not found"));

        List<Trainer> newTrainers = trainerIds.stream()
                .map(id -> trainerRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Trainer with id " + id + " not found")))
                .toList();

        trainee.getTrainers().clear();
        newTrainers.forEach(trainee::addTrainer);
        traineeRepository.update(trainee);
        log.info("Updated trainers list for trainee username={} trainerIds={}", authUsername, trainerIds);
    }

    @Transactional
    public boolean isMatching(String username, String password) {
        var trainee = traineeRepository.findByUsername(username);
        return trainee.map(value -> value.getUser().getPassword().equals(password)).orElse(false);
    }

    private void authenticate(String username, String password) {
        if (!isMatching(username, password)) {
            log.warn("Authentication failed for trainee username={}", username);
            throw new AuthenticationException("Invalid username or password for trainee " + username);
        }
    }


}
