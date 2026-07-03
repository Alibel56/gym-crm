package com.practice.service;

import com.practice.dao.TraineeRepository;
import com.practice.dao.TrainerRepository;
import com.practice.dao.TrainingRepository;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.exception.AuthenticationException;
import com.practice.exception.ValidationException;
import com.practice.utility.Criteria;
import com.practice.utility.Validator;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private TrainingRepository trainingRepository;
    private TrainerRepository trainerRepository;
    private TraineeRepository traineeRepository;

    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Transactional
    public Long create(String traineeUsername, String traineePassword, Training trainingCreate) {
        authenticateTrainee(traineeUsername, traineePassword);
        Validator.validateTrainingForCreate(trainingCreate);

        Trainer trainer = trainerRepository.findById(trainingCreate.getTrainer().getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainer's id in new training not found"));

        if (!trainer.getTrainingType().equals(trainingCreate.getTrainingType())) {
            throw new ValidationException("Trainer's specialization does not match");
        }

        Trainee trainee = traineeRepository.findByUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee's id in new training not found"));

        if (!trainee.getId().equals(trainingCreate.getTrainee().getId())) {
            throw new ValidationException("Training's trainee does not match the authenticated trainee");
        }

        Long id = trainingRepository.save(trainingCreate).getId();
        log.info("Created training id={} trainee={} trainer={}", id, traineeUsername, trainer.getUser().getUserName());
        return id;
    }

    public Optional<Training> findById(Long id) {
        return trainingRepository.findById(id);
    }

    public List<Training> findAll() {
        return trainingRepository.findAll();
    }

    public List<Training> findAllTraineeTrainingsByCriteria(String traineeUsername, String traineePassword, Criteria criteria) {
        authenticateTrainee(traineeUsername, traineePassword);
        return trainingRepository.findAllTraineeTrainingsByCriteria(traineeUsername, criteria);
    }

    public List<Training> findAllTrainerTrainingsByCriteria(String trainerUsername, String trainerPassword, Criteria criteria) {
        authenticateTrainer(trainerUsername, trainerPassword);
        return trainingRepository.findAllTrainerTrainingsByCriteria(trainerUsername, criteria);
    }

    private void authenticateTrainee(String username, String password) {
        var trainee = traineeRepository.findByUsername(username);
        boolean matches = trainee.map(t -> t.getUser().getPassword().equals(password)).orElse(false);
        if (!matches) {
            log.warn("Authentication failed for trainee username={}", username);
            throw new AuthenticationException("Invalid username or password for trainee " + username);
        }
    }

    private void authenticateTrainer(String username, String password) {
        var trainer = trainerRepository.findByUsername(username);
        boolean matches = trainer.map(t -> t.getUser().getPassword().equals(password)).orElse(false);
        if (!matches) {
            log.warn("Authentication failed for trainer username={}", username);
            throw new AuthenticationException("Invalid username or password for trainer " + username);
        }
    }
}
