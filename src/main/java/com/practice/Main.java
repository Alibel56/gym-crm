package com.practice;

import com.practice.config.AppConfig;
import com.practice.config.DataInitializer;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.TrainingType;
import com.practice.facade.GymFacade;
import com.practice.runner.DemoRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            context.getBean(DataInitializer.class).init();
            GymFacade facade = context.getBean(GymFacade.class);
            DemoRunner runner = new DemoRunner(facade);

            TrainingType trainingType = runner.pickTrainingType();
            section("0. Using training type: " + trainingType.getName());

            section("1-2. Create Trainee / Trainer profiles");
            DemoRunner.CreatedTrainee createdTrainee = runner.createTrainee();
            Trainee trainee = createdTrainee.trainee();
            String traineeUsername = trainee.getUser().getUserName();
            String traineePassword = trainee.getUser().getPassword();
            System.out.printf("Created trainee id=%d username=%s password=%s%n",
                    createdTrainee.id(), traineeUsername, traineePassword);

            DemoRunner.CreatedTrainer createdTrainer = runner.createTrainer(trainingType);
            Trainer trainer = createdTrainer.trainer();
            String trainerUsername = trainer.getUser().getUserName();
            String trainerPassword = trainer.getUser().getPassword();
            System.out.printf("Created trainer id=%d username=%s password=%s%n",
                    createdTrainer.id(), trainerUsername, trainerPassword);

            section("3-4. Credentials matching");
            System.out.println("Trainee credentials OK: " + runner.isTraineeMatching(traineeUsername, traineePassword));
            System.out.println("Trainer credentials OK: " + runner.isTrainerMatching(trainerUsername, trainerPassword));
            System.out.println("Trainee with WRONG password: " + runner.isTraineeMatching(traineeUsername, "wrong-password"));

            section("5-6. Select profiles by username");
            Optional<Trainee> foundTrainee = runner.findTraineeByUsername(traineeUsername, traineePassword);
            System.out.println("Found trainee: " + foundTrainee.map(t -> t.getUser().getFirstName()).orElse("N/A"));

            Optional<Trainer> foundTrainer = runner.findTrainerByUsername(trainerUsername, trainerPassword);
            System.out.println("Found trainer: " + foundTrainer.map(t -> t.getUser().getFirstName()).orElse("N/A"));

            section("7-8. Password change");
            String traineeNewPassword = "NewTraineePass1";
            runner.changeTraineePassword(traineeUsername, traineePassword, traineeNewPassword);
            System.out.println("Trainee password changed, old password now rejected: "
                    + !runner.isTraineeMatching(traineeUsername, traineePassword));
            System.out.println("Trainee new password accepted: "
                    + runner.isTraineeMatching(traineeUsername, traineeNewPassword));
            traineePassword = traineeNewPassword;

            String trainerNewPassword = "NewTrainerPass1";
            runner.changeTrainerPassword(trainerUsername, trainerPassword, trainerNewPassword);
            System.out.println("Trainer new password accepted: "
                    + runner.isTrainerMatching(trainerUsername, trainerNewPassword));
            trainerPassword = trainerNewPassword;

            section("9-10. Update profiles");
            runner.renameTraineeFirstName(trainee, "Jonathan", traineeUsername, traineePassword);
            System.out.println("Trainee first name updated to: "
                    + runner.findTraineeByUsername(traineeUsername, traineePassword)
                    .map(t -> t.getUser().getFirstName()).orElse("N/A"));

            runner.renameTrainerFirstName(trainer, "Annabelle", trainerUsername, trainerPassword);
            System.out.println("Trainer first name updated to: "
                    + runner.findTrainerByUsername(trainerUsername, trainerPassword)
                    .map(t -> t.getUser().getFirstName()).orElse("N/A"));

            section("11-12. Activate/Deactivate (toggle, not idempotent)");
            runner.toggleTraineeActive(traineeUsername, traineePassword);
            System.out.println("Trainee isActive after 1st toggle: "
                    + runner.findTraineeByUsername(traineeUsername, traineePassword)
                    .map(t -> t.getUser().getIsActive()).orElse(null));
            runner.toggleTraineeActive(traineeUsername, traineePassword);
            System.out.println("Trainee isActive after 2nd toggle: "
                    + runner.findTraineeByUsername(traineeUsername, traineePassword)
                    .map(t -> t.getUser().getIsActive()).orElse(null));

            runner.toggleTrainerActive(trainerUsername, trainerPassword);
            System.out.println("Trainer isActive after toggle: "
                    + runner.findTrainerByUsername(trainerUsername, trainerPassword)
                    .map(t -> t.getUser().getIsActive()).orElse(null));

            section("17-18. Not-assigned trainers / update trainee's trainers list");
            List<Trainer> notAssigned = runner.findAllTrainersNotAssignedToTrainee(traineeUsername, traineePassword);
            System.out.println("Trainers not yet assigned to trainee: " + notAssigned.size());

            runner.assignTrainerToTrainee(traineeUsername, traineePassword, createdTrainer.id());
            System.out.println("Assigned trainers after update: "
                    + runner.findTraineeByUsername(traineeUsername, traineePassword)
                    .map(t -> t.getTrainers().size()).orElse(-1));

            section("16. Add training");
            Long trainingId = runner.createTraining(trainee, trainer, trainingType, traineeUsername, traineePassword);
            System.out.println("Created training id=" + trainingId);

            section("14-15. Trainings by criteria");
            System.out.println("Trainee trainings found: " + runner.findTraineeTrainings(traineeUsername, traineePassword).size());
            System.out.println("Trainer trainings found: " + runner.findTrainerTrainings(trainerUsername, trainerPassword).size());

            section("13. Delete trainee profile by username");
            boolean deleted = runner.deleteTrainee(traineeUsername, traineePassword);
            System.out.println("Trainee deleted: " + deleted);
            System.out.println("Trainee lookup after delete throws AuthenticationException (expected): ");
            try {
                runner.findTraineeByUsername(traineeUsername, traineePassword);
            } catch (Exception e) {
                System.out.println("  -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }

            section("Demo finished");
        }
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("===== " + title + " =====");
    }
}