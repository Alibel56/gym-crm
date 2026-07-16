package com.practice.dao;

import com.practice.entity.Training;
import com.practice.utility.Criteria;

import java.util.List;
import java.util.Optional;

public interface TrainingRepository {
    Training save(Training training);
    Optional<Training> findById(Long id);
    List<Training> findAllTrainerTrainingsByCriteria(String username, Criteria criteria);
    List<Training> findAllTraineeTrainingsByCriteria(String username, Criteria criteria);
    List<Training> findAll();
}
