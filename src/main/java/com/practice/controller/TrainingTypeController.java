package com.practice.controller;

import com.practice.dto.response.GetTrainingTypesResponse;
import com.practice.service.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Training Type", description = "Operations related to training types")
@RestController
@RequestMapping("/api/training-types")
public class TrainingTypeController {

    private TrainingTypeService trainingTypeService;

    @Autowired
    public void setTrainingTypeService(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    @Operation(
            summary = "Get training types",
            description = "Returns the list of available training types. This is a read-only reference list and cannot be modified through the API."
    )
    @GetMapping
    public ResponseEntity<GetTrainingTypesResponse> getTrainingTypes() {
        return ResponseEntity.ok(
                new GetTrainingTypesResponse(trainingTypeService.findAll())
        );
    }
}