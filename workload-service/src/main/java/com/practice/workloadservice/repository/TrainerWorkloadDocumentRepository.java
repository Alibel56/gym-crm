package com.practice.workloadservice.repository;

import com.practice.workloadservice.model.TrainerWorkloadDocument;

import java.util.Optional;

public interface TrainerWorkloadDocumentRepository {

    Optional<TrainerWorkloadDocument> findByUsername(String username);

    TrainerWorkloadDocument save(TrainerWorkloadDocument document);
}
