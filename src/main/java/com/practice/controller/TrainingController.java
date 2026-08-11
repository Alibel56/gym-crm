package com.practice.controller;

import com.practice.dto.request.CreateTrainingRequest;
import com.practice.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Training", description = "Operations related to trainings")
@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    private TrainingService trainingService;

    @Autowired
    public void setTrainingService(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @Operation(
            summary = "Add training",
            description = "Creates a new training. Trainings cannot be updated via the REST API."
    )
    @PostMapping
    public ResponseEntity<Void> addTraining(
            @Valid @RequestBody CreateTrainingRequest request) {

        trainingService.create(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Cancel training",
            description = "Deletes (cancels) a planned training and notifies the workload service."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTraining(@PathVariable Long id) {
        trainingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}