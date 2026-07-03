package com.practice.facade;

import com.practice.dao.TrainingTypeRepository;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.TrainingType;
import com.practice.service.TraineeService;
import com.practice.service.TrainerService;
import com.practice.service.TrainingService;
import com.practice.utility.Criteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeRepository trainingTypeRepository;

    public long createTrainee(Trainee traineeCreate) {
        return traineeService.create(traineeCreate);
    }

    public boolean updateTrainee(String authUsername, String authPassword, Trainee traineeUpdate) {
        return traineeService.update(authUsername, authPassword, traineeUpdate);
    }

    public boolean deleteTrainee(String authUsername, String authPassword) {
        return traineeService.delete(authUsername, authPassword);
    }

    public Optional<Trainee> findTraineeById(Long id) {
        return traineeService.findById(id);
    }

    public Optional<Trainee> findTraineeByUsername(String authUsername, String authPassword) {
        return traineeService.findByUsername(authUsername, authPassword);
    }

    public List<Trainee> findAllTrainees() {
        return traineeService.findAll();
    }

    public void switchActiveTrainee(String authUsername, String authPassword) {
        traineeService.switchActive(authUsername, authPassword);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        traineeService.changePassword(username, oldPassword, newPassword);
    }

    public void updateTraineeTrainersList(String authUsername, String authPassword, List<Long> trainerIds) {
        traineeService.updateTrainersList(authUsername, authPassword, trainerIds);
    }

    public boolean isTraineeMatching(String username, String password) {
        return traineeService.isMatching(username, password);
    }

    public long createTrainer(Trainer trainerCreate) {
        return trainerService.create(trainerCreate);
    }

    public boolean updateTrainer(String authUsername, String authPassword, Trainer trainerUpdate) {
        return trainerService.update(authUsername, authPassword, trainerUpdate);
    }

    public Optional<Trainer> findTrainerById(Long id) {
        return trainerService.findById(id);
    }

    public List<Trainer> findAllTrainers() {
        return trainerService.findAll();
    }

    public Optional<Trainer> findTrainerByUsername(String authUsername, String authPassword) {
        return trainerService.findByUsername(authUsername, authPassword);
    }

    public List<Trainer> findAllTrainersNotAssignedToTrainee(String traineeUsername, String traineePassword) {
        return trainerService.findAllNotAssignedByTraineeUserName(traineeUsername, traineePassword);
    }

    public void switchActiveTrainer(String authUsername, String authPassword) {
        trainerService.switchActive(authUsername, authPassword);
    }

    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        trainerService.changePassword(username, oldPassword, newPassword);
    }

    public boolean isTrainerMatching(String username, String password) {
        return trainerService.isMatching(username, password);
    }

    public Long createTraining(String traineeUsername, String traineePassword, Training trainingCreate) {
        return trainingService.create(traineeUsername, traineePassword, trainingCreate);
    }

    public Optional<Training> findTrainingById(Long id) {
        return trainingService.findById(id);
    }

    public List<Training> findAllTrainings() {
        return trainingService.findAll();
    }

    public List<Training> findAllTraineeTrainingsByCriteria(String traineeUsername, String traineePassword, Criteria criteria) {
        return trainingService.findAllTraineeTrainingsByCriteria(traineeUsername, traineePassword, criteria);
    }

    public List<Training> findAllTrainerTrainingsByCriteria(String trainerUsername, String trainerPassword, Criteria criteria) {
        return trainingService.findAllTrainerTrainingsByCriteria(trainerUsername, trainerPassword, criteria);
    }

    @Transactional(readOnly = true)
    public TrainingType findAnyTrainingType() {
        return trainingTypeRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No rows in training_types - seed at least one before running this demo"));
    }


}
