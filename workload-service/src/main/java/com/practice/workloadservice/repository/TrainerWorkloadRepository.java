package com.practice.workloadservice.repository;

import com.practice.workloadservice.model.TrainerWorkload;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class TrainerWorkloadRepository {
    private final ConcurrentMap<String, TrainerWorkload> storage = new ConcurrentHashMap<>();

    public TrainerWorkload getOrCreate(String username, String firstName, String lastName, boolean active) {
        TrainerWorkload workload = storage.computeIfAbsent(username,
                ignored -> new TrainerWorkload(username, firstName, lastName, active));
        workload.updateIdentity(firstName, lastName, active);
        return workload;
    }

    public Optional<TrainerWorkload> findByUsername(String username) {
        return Optional.ofNullable(storage.get(username));
    }
}
