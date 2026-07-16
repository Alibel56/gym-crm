package com.practice.dao;

import com.practice.entity.Trainee;
import com.practice.utility.Criteria;

import java.util.List;
import java.util.Optional;

public interface TraineeRepository {
    Trainee save(Trainee trainee);
    Optional<Trainee> findById(Long id);
    Optional<Trainee> findByUsername(String username);
    List<Trainee> findAll();
    boolean delete(String username);
    boolean isActive(Long id);

}
