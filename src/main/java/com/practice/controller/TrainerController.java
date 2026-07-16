package com.practice.controller;

import com.practice.dto.request.GetTrainerTrainingsListRequest;
import com.practice.dto.request.SwitchActiveFlagRequest;
import com.practice.dto.request.UpdateTrainerProfileRequest;
import com.practice.dto.request.UsernameRequest;
import com.practice.dto.response.GetTrainerProfileResponse;
import com.practice.dto.response.GetTrainerTrainingsListResponse;
import com.practice.dto.response.UpdateTrainerProfileResponse;
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

@Tag(name = "Trainer", description = "Operations related to trainer management")
@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    private TrainerService trainerService;
    private TrainingService trainingService;

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Autowired
    public void setTrainingService(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @Operation(summary = "Get trainer profile")
    @GetMapping("/{userName}")
    public ResponseEntity<GetTrainerProfileResponse> getProfile(
            @PathVariable(name = "userName") String userName) {

        return ResponseEntity.ok(
                trainerService.findByUsername(new UsernameRequest(userName)));
    }

    @Operation(
            summary = "Update trainer profile",
            description = "Updates trainer profile information. Specialization is read-only and cannot be changed."
    )
    @PutMapping
    public ResponseEntity<UpdateTrainerProfileResponse> updateProfile(
            @Valid @RequestBody UpdateTrainerProfileRequest request) {

        return ResponseEntity.ok(trainerService.update(request));
    }

    @Operation(
            summary = "Activate or deactivate trainer",
            description = "Switches the active status of a trainer. This operation is not idempotent."
    )
    @PatchMapping("/status")
    public ResponseEntity<Void> switchActive(
            @Valid @RequestBody SwitchActiveFlagRequest request) {

        trainerService.switchActive(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userName}/trainings")
    public ResponseEntity<List<GetTrainerTrainingsListResponse>> getTrainings(
            @PathVariable("userName") String userName,

            @RequestParam(value = "periodFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodFrom,

            @RequestParam(value = "periodTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodTo,

            @RequestParam(value = "traineeName", required = false)
            String traineeName
    ) {
        GetTrainerTrainingsListRequest request =
                new GetTrainerTrainingsListRequest(
                        userName,
                        periodFrom,
                        periodTo,
                        traineeName
                );

        return ResponseEntity.ok(
                trainingService.findAllTrainerTrainings(request)
        );
    }
}