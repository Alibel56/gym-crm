package com.practice.dao;

import com.practice.entity.Training;
import com.practice.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class TrainingDao {

    private Storage storage;

    @Autowired
    public void setStorage(Storage storage) {
        this.storage = storage;
        log.debug("TrainingDao - Storage injected");
    }

    public Training create(Training training){
        Long newId = storage.getTrainings()
                .keySet()
                .stream()
                .max(Long::compareTo)
                .orElse(0L) + 1;

        training.setId(newId);
        storage.getTrainings().put(newId, training);
        log.info("TrainingDao - New Training successfully created with id {}", newId);
        return training;
    }

    public Optional<Training> findById(Long id){
        log.debug("TrainingDao - Training successfully read with id {}", id);
        return Optional.ofNullable(storage.getTrainings().get(id));
    }

    public List<Training> findAll(){
        log.debug("TrainingDao - All Trainings successfully read");
        return new ArrayList<>(storage.getTrainings().values());
    }
}
