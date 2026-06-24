package com.practice.dao;

import com.practice.entity.Trainee;
import com.practice.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class TraineeDao {

    private Storage storage;

    @Autowired
    public void setStorage(Storage storage) {
        this.storage = storage;
        log.debug("TraineeDao - Storage injected");
    }

    public Trainee create(Trainee trainee){
        Long newId = storage.getTrainees()
                .keySet()
                .stream()
                .max(Long::compareTo)
                .orElse(0L) + 1;

        trainee.setId(newId);
        trainee.setActive(true);
        storage.getTrainees().put(newId, trainee);
        log.info("TraineeDao - New Trainee successfully created with id {}", newId);
        return trainee;
    }

    public void update(Trainee trainee){
        storage.getTrainees().put(trainee.getId(),trainee);
        log.info("TraineeDao - Trainee successfully updated with id {}", trainee.getId());
    }

    public void delete(Long id){
        storage.getTrainees().remove(id);
        log.info("TraineeDao - Trainee successfully deleted with id {}", id);
    }

    public Optional<Trainee> findById(Long id){
        log.debug("TraineeDao - Trainee successfully read with id {}", id);
        return Optional.ofNullable(storage.getTrainees().get(id));
    }

    public List<Trainee> findAll(){
        log.debug("TraineeDao - All Trainees successfully read");
        return new ArrayList<>(storage.getTrainees().values());
    }
}
