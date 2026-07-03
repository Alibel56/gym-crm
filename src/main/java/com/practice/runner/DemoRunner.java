package com.practice.runner;

import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.TrainingType;
import com.practice.entity.User;
import com.practice.facade.GymFacade;
import com.practice.utility.Criteria;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public class DemoRunner {

    private final GymFacade facade;

    public DemoRunner(GymFacade facade) {
        this.facade = facade;
    }

    public TrainingType pickTrainingType() {
        return facade.findAnyTrainingType();
    }

    public record CreatedTrainee(long id, Trainee trainee) {}
    public record CreatedTrainer(long id, Trainer trainer) {}

    public CreatedTrainee createTrainee() {
        Trainee trainee = Trainee.builder()
                .user(User.builder().firstName("John").lastName("Smith").build())
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .address("221B Baker Street")
                .build();
        long id = facade.createTrainee(trainee);
        return new CreatedTrainee(id, trainee);
    }

    public CreatedTrainer createTrainer(TrainingType trainingType) {
        Trainer trainer = Trainer.builder()
                .user(User.builder().firstName("Anna").lastName("Lee").build())
                .trainingType(trainingType)
                .build();
        long id = facade.createTrainer(trainer);
        return new CreatedTrainer(id, trainer);
    }

    public boolean isTraineeMatching(String username, String password) {
        return facade.isTraineeMatching(username, password);
    }

    public boolean isTrainerMatching(String username, String password) {
        return facade.isTrainerMatching(username, password);
    }

    public Optional<Trainee> findTraineeByUsername(String username, String password) {
        return facade.findTraineeByUsername(username, password);
    }

    public Optional<Trainer> findTrainerByUsername(String username, String password) {
        return facade.findTrainerByUsername(username, password);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        facade.changeTraineePassword(username, oldPassword, newPassword);
    }

    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        facade.changeTrainerPassword(username, oldPassword, newPassword);
    }

    public void renameTraineeFirstName(Trainee trainee, String newFirstName, String username, String password) {
        trainee.getUser().setFirstName(newFirstName);
        facade.updateTrainee(username, password, trainee);
    }

    public void renameTrainerFirstName(Trainer trainer, String newFirstName, String username, String password) {
        trainer.getUser().setFirstName(newFirstName);
        facade.updateTrainer(username, password, trainer);
    }

    public void toggleTraineeActive(String username, String password) {
        facade.switchActiveTrainee(username, password);
    }

    public void toggleTrainerActive(String username, String password) {
        facade.switchActiveTrainer(username, password);
    }

    public List<Trainer> findAllTrainersNotAssignedToTrainee(String username, String password) {
        return facade.findAllTrainersNotAssignedToTrainee(username, password);
    }

    public void assignTrainerToTrainee(String username, String password, long trainerId) {
        facade.updateTraineeTrainersList(username, password, List.of(trainerId));
    }

    public Long createTraining(Trainee trainee, Trainer trainer, TrainingType trainingType,
                               String username, String password) {
        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Morning Cardio")
                .trainingType(trainingType)
                .trainingDate(LocalDate.now())
                .trainingDuration(60)
                .build();
        return facade.createTraining(username, password, training);
    }

    public List<Training> findTraineeTrainings(String username, String password) {
        Criteria criteria = Criteria.builder()
                .fromDate(LocalDate.now().minusDays(1))
                .toDate(LocalDate.now().plusDays(1))
                .build();
        return facade.findAllTraineeTrainingsByCriteria(username, password, criteria);
    }

    public List<Training> findTrainerTrainings(String username, String password) {
        Criteria criteria = Criteria.builder()
                .fromDate(LocalDate.now().minusDays(1))
                .toDate(LocalDate.now().plusDays(1))
                .build();
        return facade.findAllTrainerTrainingsByCriteria(username, password, criteria);
    }

    public boolean deleteTrainee(String username, String password) {
        return facade.deleteTrainee(username, password);
    }
}