package com.practice.dao;

import com.practice.entity.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeRepository {

    public Optional<TrainingType> findById(Long id);
    public List<TrainingType> findAll();
    public void save(TrainingType fitness);
}
