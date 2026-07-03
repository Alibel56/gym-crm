package com.practice.service;

import com.practice.dao.TraineeRepositoryImpl;
import com.practice.dao.TrainerRepositoryImpl;
import com.practice.dao.TrainingTypeRepository;
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

@Service
@Transactional(readOnly = true)
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private TrainerRepositoryImpl trainerRepository;
    private TraineeRepositoryImpl traineeRepository;
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    public void setTrainerRepository(TrainerRepositoryImpl trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setTraineeRepository(TraineeRepositoryImpl traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Transactional
    public long create(Trainer trainerCreate) {
        Validator.validateTrainerForCreate(trainerCreate);

        boolean exists = trainingTypeRepository.findAll().stream()
                .anyMatch(t -> t.equals(trainerCreate.getTrainingType()));

        if (!exists) {
            throw new IllegalArgumentException("TrainingType does not exist");
        }

        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(traineeRepository.findAll().stream().map(Trainee::getUser).toList());
        allUsers.addAll(trainerRepository.findAll().stream().map(Trainer::getUser).toList());

        String userName = ServiceUtility.generateUserName(
                trainerCreate.getUser().getFirstName(),
                trainerCreate.getUser().getLastName(),
                allUsers);
        String password = ServiceUtility.generatePassword();

        trainerCreate.getUser().setUserName(userName);
        trainerCreate.getUser().setPassword(password);
        trainerCreate.getUser().setIsActive(true);

        Long id = trainerRepository.save(trainerCreate).getId();
        log.info("Created trainer profile id={} username={}", id, userName);
        return id;
    }

    @Transactional
    public boolean update(String authUsername, String authPassword, Trainer trainerUpdate) {
        authenticate(authUsername, authPassword);
        Validator.validateTrainerForCreate(trainerUpdate);

        var maybeEntity = trainerRepository.findById(trainerUpdate.getId());
        maybeEntity.ifPresentOrElse(
                t -> trainerRepository.update(trainerUpdate),
                () -> { throw new EntityNotFoundException("Trainer with id " + trainerUpdate.getId() + " not found"); }
        );
        log.info("Updated trainer id={}", trainerUpdate.getId());
        return maybeEntity.isPresent();
    }

    public Optional<Trainer> findById(Long id) {
        return trainerRepository.findById(id);
    }

    public List<Trainer> findAll(){
        return trainerRepository.findAll();
    }

    public Optional<Trainer> findByUsername(String authUsername, String authPassword) {
        authenticate(authUsername, authPassword);
        return trainerRepository.findByUsername(authUsername);
    }

    public List<Trainer> findAllNotAssignedByTraineeUserName(String traineeUsername, String traineePassword) {
        authenticateTrainee(traineeUsername, traineePassword);
        return trainerRepository.findAllNotAssignedByTraineeUserName(traineeUsername);
    }

    @Transactional
    public void switchActive(String authUsername, String authPassword) {
        authenticate(authUsername, authPassword);
        Trainer trainer = trainerRepository.findByUsername(authUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainer " + authUsername + " not found"));
        trainerRepository.switchActive(trainer.getId());
        log.info("Toggled isActive for trainer username={}", authUsername);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        authenticate(username, oldPassword);
        Validator.requireNotBlank(newPassword, "newPassword");
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer " + username + " not found"));
        trainer.getUser().setPassword(newPassword);
        trainerRepository.update(trainer);
        log.info("Password changed for trainer username={}", username);
    }

    @Transactional
    public boolean isMatching(String username, String password) {
        var trainer = trainerRepository.findByUsername(username);
        return trainer.map(value -> value.getUser().getPassword().equals(password)).orElse(false);
    }

    private void authenticate(String username, String password) {
        if (!isMatching(username, password)) {
            log.warn("Authentication failed for trainer username={}", username);
            throw new AuthenticationException("Invalid username or password for trainer " + username);
        }
    }

    private void authenticateTrainee(String username, String password) {
        var trainee = traineeRepository.findByUsername(username);
        boolean matches = trainee.map(t -> t.getUser().getPassword().equals(password)).orElse(false);
        if (!matches) {
            log.warn("Authentication failed for trainee username={}", username);
            throw new AuthenticationException("Invalid username or password for trainee " + username);
        }
    }
}
