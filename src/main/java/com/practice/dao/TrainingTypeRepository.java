package com.practice.dao;

import com.practice.entity.TrainingType;

import java.util.List;

public interface TrainingTypeRepository {

    public List<TrainingType> findAll();
    public void save(TrainingType fitness);
}
