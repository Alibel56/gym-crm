package com.practice.workloadservice.service;

import com.practice.workloadservice.dto.*;
import com.practice.workloadservice.exception.WorkloadNotFoundException;
import com.practice.workloadservice.model.TrainerWorkload;
import com.practice.workloadservice.repository.TrainerWorkloadRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository repository;

    public TrainerWorkloadService(TrainerWorkloadRepository repository) {
        this.repository = repository;
    }

    public void update(TrainerWorkloadRequest request) {
        TrainerWorkload workload = repository.getOrCreate(
                request.trainerUsername(),
                request.trainerFirstName(),
                request.trainerLastName(),
                request.isActive());

        LocalDate date = request.trainingDate();
        int delta = request.actionType() == ActionType.ADD
                ? request.trainingDuration()
                : -request.trainingDuration();

        synchronized (workload) {
            Map<Integer, Integer> months = workload.getYears()
                    .computeIfAbsent(date.getYear(), ignored -> new java.util.concurrent.ConcurrentHashMap<>());

            int updatedDuration = months.getOrDefault(date.getMonthValue(), 0) + delta;
            if (updatedDuration < 0) {
                throw new IllegalArgumentException("Training summary duration cannot become negative");
            }

            if (updatedDuration == 0) {
                months.remove(date.getMonthValue());
                if (months.isEmpty()) {
                    workload.getYears().remove(date.getYear());
                }
            } else {
                months.put(date.getMonthValue(), updatedDuration);
            }
        }
    }

    public TrainerWorkloadResponse getByUsername(String username) {
        TrainerWorkload workload = find(username);
        List<YearSummaryResponse> years = workload.getYears().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(year -> new YearSummaryResponse(
                        year.getKey(),
                        year.getValue().entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .map(month -> new MonthSummaryResponse(month.getKey(), month.getValue()))
                                .toList()))
                .toList();

        return new TrainerWorkloadResponse(
                workload.getUsername(),
                workload.getFirstName(),
                workload.getLastName(),
                workload.isActive(),
                years);
    }

    public MonthlyWorkloadResponse getMonthly(String username, int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        TrainerWorkload workload = find(username);
        int duration = workload.getYears()
                .getOrDefault(year, Map.of())
                .getOrDefault(month, 0);
        return new MonthlyWorkloadResponse(username, year, month, duration);
    }

    private TrainerWorkload find(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new WorkloadNotFoundException("Trainer workload not found: " + username));
    }
}
