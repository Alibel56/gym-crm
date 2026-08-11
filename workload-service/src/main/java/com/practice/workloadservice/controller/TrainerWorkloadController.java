package com.practice.workloadservice.controller;

import com.practice.workloadservice.dto.MonthlyWorkloadResponse;
import com.practice.workloadservice.dto.TrainerWorkloadRequest;
import com.practice.workloadservice.dto.TrainerWorkloadResponse;
import com.practice.workloadservice.service.TrainerWorkloadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workloads")
public class TrainerWorkloadController {

    private final TrainerWorkloadService service;

    public TrainerWorkloadController(TrainerWorkloadService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> updateWorkload(@Valid @RequestBody TrainerWorkloadRequest request) {
        service.update(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerWorkloadResponse> getWorkload(@PathVariable String username) {
        return ResponseEntity.ok(service.getByUsername(username));
    }

    @GetMapping("/{username}/monthly")
    public ResponseEntity<MonthlyWorkloadResponse> getMonthlyWorkload(
            @PathVariable String username,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(service.getMonthly(username, year, month));
    }
}
