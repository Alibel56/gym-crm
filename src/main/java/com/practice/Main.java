package com.practice;


import com.practice.config.AppConfig;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.TrainingType;
import com.practice.facade.GymFacade;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Duration;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            GymFacade gymFacade = context.getBean(GymFacade.class);

            // =========================
            // CREATE TRAINEES
            // =========================

            Trainee trainee1 = new Trainee();
            trainee1.setFirstName("John");
            trainee1.setLastName("Smith");
            trainee1.setDateOfBirth(LocalDate.of(2001,1,1));
            trainee1.setAddress("Avenue 1");

            Trainee trainee2 = new Trainee();
            trainee2.setFirstName("Mike");
            trainee2.setLastName("Johnson");
            trainee2.setDateOfBirth(LocalDate.of(2002,2,2));
            trainee2.setAddress("Avenue 2");

            Long traineeId1 = gymFacade.createTrainee(trainee1);
            Long traineeId2 = gymFacade.createTrainee(trainee2);

            System.out.println("Created trainees:");
            System.out.println(traineeId1);
            System.out.println(traineeId2);

            // =========================
            // CREATE TRAINERS
            // =========================

            Trainer trainer1 = new Trainer();
            trainer1.setFirstName("Alex");
            trainer1.setLastName("Brown");
            trainer1.setSpecialization(TrainingType.builder().trainingTypeName("SWIM").build());

            Trainer trainer2 = new Trainer();
            trainer2.setFirstName("David");
            trainer2.setLastName("Wilson");
            trainer2.setSpecialization(TrainingType.builder().trainingTypeName("CARDIO").build());

            Long trainerId1 = gymFacade.createTrainer(trainer1);
            Long trainerId2 = gymFacade.createTrainer(trainer2);

            System.out.println("\nCreated trainers:");
            System.out.println(trainerId1);
            System.out.println(trainerId2);

            // =========================
            // CREATE TRAININGS
            // =========================

            Training training1 = Training.builder()
                    .traineeId(1L)
                    .trainerId(4L)
                    .trainingName("CARDIO day")
                    .trainingType(TrainingType.builder().trainingTypeName("CARDIO").build())
                    .trainingDate(LocalDate.of(2026, 6, 25))
                    .trainingDuration(Duration.ofHours(1))
                    .build();

            Training training2 = Training.builder()
                    .traineeId(2L)
                    .trainerId(3L)
                    .trainingName("SWIM")
                    .trainingType(TrainingType.builder().trainingTypeName("SWIM").build())
                    .trainingDate(LocalDate.of(2026, 6, 25))
                    .trainingDuration(Duration.ofHours(1))
                    .build();

            Long trainingId1 = gymFacade.createTraining(training1);
            Long trainingId2 = gymFacade.createTraining(training2);

            System.out.println("\nCreated trainings:");
            System.out.println(trainingId1);
            System.out.println(trainingId2);

            // =========================
            // FIND BY ID
            // =========================

            System.out.println("\nFind trainee by id:");
            gymFacade.findByIdTrainee(traineeId1)
                    .ifPresent(System.out::println);

            System.out.println("\nFind trainer by id:");
            gymFacade.findByIdTrainer(trainerId1)
                    .ifPresent(System.out::println);

            System.out.println("\nFind training by id:");
            gymFacade.findByIdTraining(trainingId1)
                    .ifPresent(System.out::println);

            // =========================
            // UPDATE TRAINEE
            // =========================

            trainee1.setId(traineeId1);
            trainee1.setFirstName("UpdatedJohn");
            trainee1.setLastName("UpdatedSmith");

            boolean traineeUpdated =
                    gymFacade.updateTrainee(trainee1);

            System.out.println("\nTrainee updated = " + traineeUpdated);

            // =========================
            // UPDATE TRAINER
            // =========================

            trainer1.setId(trainerId1);
            trainer1.setFirstName("UpdatedAlex");
            trainer1.setLastName("UpdatedBrown");

            boolean trainerUpdated =
                    gymFacade.updateTrainer(trainer1);

            System.out.println("Trainer updated = " + trainerUpdated);

            // =========================
            // SHOW ALL
            // =========================

            System.out.println("\n=== ALL TRAINEES ===");
            gymFacade.findAllTrainee()
                    .forEach(System.out::println);

            System.out.println("\n=== ALL TRAINERS ===");
            gymFacade.findAllTrainer()
                    .forEach(System.out::println);

            System.out.println("\n=== ALL TRAININGS ===");
            gymFacade.findAllTraining()
                    .forEach(System.out::println);

            // =========================
            // DELETE TRAINEE
            // =========================

            boolean deleted =
                    gymFacade.deleteTrainee(traineeId2);

            System.out.println("\nDeleted trainee = " + deleted);

            // =========================
            // SHOW ALL AGAIN
            // =========================

            System.out.println("\n=== TRAINEES AFTER DELETE ===");
            gymFacade.findAllTrainee()
                    .forEach(System.out::println);
        }
    }
}