package com.practice.controller;

import com.practice.dto.request.*;
import com.practice.dto.response.GetTraineeProfileResponse;
import com.practice.dto.response.GetTraineeTrainingsListResponse;
import com.practice.dto.response.TrainerSummaryResponse;
import com.practice.dto.response.UpdateTraineeProfileResponse;
import com.practice.service.TraineeService;
import com.practice.service.TrainerService;
import com.practice.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Trainee", description = "Operations related to trainee management")
@RestController
@RequestMapping("/api/trainees")
public class TraineeController {

    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingService trainingService;

    @Autowired
    public void setTraineeService(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Autowired
    public void setTrainingService(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @Operation(summary = "Get trainee profile")
    @GetMapping("/{userName}")
    public ResponseEntity<GetTraineeProfileResponse> getProfile(
            @PathVariable(name = "userName") String userName) {

        return ResponseEntity.ok(
                traineeService.findByUsername(new UsernameRequest(userName)));
    }

    @Operation(summary = "Update trainee profile")
    @PutMapping
    public ResponseEntity<UpdateTraineeProfileResponse> updateProfile(
            @Valid @RequestBody UpdateTraineeProfileRequest request) {

        return ResponseEntity.ok(traineeService.update(request));
    }

    @Operation(
            summary = "Delete trainee profile",
            description = "Deletes a trainee profile together with all associated trainings."
    )
    @DeleteMapping("/{userName}")
    public ResponseEntity<Void> deleteProfile(
            @PathVariable(name = "userName") String userName) {

        traineeService.delete(new UsernameRequest(userName));
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Activate or deactivate trainee",
            description = "Switches the active status of a trainee. This operation is not idempotent."
    )
    @PatchMapping("/status")
    public ResponseEntity<Void> switchActive(
            @Valid @RequestBody SwitchActiveFlagRequest request) {

        traineeService.switchActive(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get active trainers not assigned to trainee")
    @GetMapping("/{userName}/unassigned-trainers")
    public ResponseEntity<List<TrainerSummaryResponse>> getUnassignedTrainers(
            @PathVariable(name = "userName") String userName) {

        return ResponseEntity.ok(
                trainerService.findAllNotAssignedByTraineeUserName(
                        new UsernameRequest(userName)));
    }

    @Operation(summary = "Update trainee's trainer list")
    @PutMapping("/trainers")
    public ResponseEntity<List<TrainerSummaryResponse>> updateTrainersList(
            @Valid @RequestBody UpdateTraineeTrainerListRequest request) {

        return ResponseEntity.ok(
                traineeService.updateTrainersList(request));
    }

    @GetMapping("/{userName}/trainings")
    public ResponseEntity<List<GetTraineeTrainingsListResponse>> getTrainings(
            @PathVariable("userName")  String userName,

            @RequestParam(value = "periodFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodFrom,

            @RequestParam(value = "periodTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodTo,

            @RequestParam(value = "trainerName", required = false)
            String trainerName,

            @RequestParam(value = "trainingType", required = false)
            String trainingType
    ) {
        GetTraineeTrainingsListRequest request =
                new GetTraineeTrainingsListRequest(
                        userName,
                        periodFrom,
                        periodTo,
                        trainerName,
                        trainingType
                );

        return ResponseEntity.ok(
                trainingService.findAllTraineeTrainings(request)
        );
    }
}