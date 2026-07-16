package com.practice.dao;

import com.practice.entity.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository {
    Trainer save(Trainer trainer);
    Optional<Trainer> findById(Long id);
    List<Trainer> findAll();
    Optional<Trainer> findByUsername(String username);
    List<Trainer> findAllNotAssignedByTraineeUserName(String username);
    void switchActive(Long id);
    boolean isActive(Long id);
}
