package com.practice.dao;

import com.practice.entity.Trainer;
import com.practice.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class TrainerDao {

    private Storage storage;

    @Autowired
    public void setStorage(Storage storage) {
        this.storage = storage;
        log.debug("Storage injected");
    }

    public Trainer create(Trainer trainer){
        Long newId = storage.getTrainers()
                .keySet()
                .stream()
                .max(Long::compareTo)
                .orElse(0L) + 1;

        trainer.setId(newId);
        trainer.setActive(true);
        storage.getTrainers().put(newId, trainer);
        log.info("New Trainer successfully created with id {}", newId);
        return trainer;
    }

    public void update(Trainer trainer){
        storage.getTrainers().put(trainer.getId(), trainer);
        log.info("Trainer successfully updated with id {}", trainer.getId());
    }

    public Optional<Trainer> findById(Long id){
        log.debug("Trainer successfully read with id {}", id);
        return Optional.ofNullable(storage.getTrainers().get(id));
    }

    public List<Trainer> findAll(){
        log.debug("All Trainers successfully read");
        return new ArrayList<>(storage.getTrainers().values());
    }

}
