package com.practice.dao;

import com.practice.entity.TrainingType;
import com.practice.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class TrainingTypeDao {

    private Storage storage;

    @Autowired
    public void setStorage(Storage storage) {
        this.storage = storage;
        log.debug("TrainingTypeDao - Storage injected");
    }

    public List<TrainingType> findAll(){
        log.debug("TrainingTypeDao - All TrainingTypes successfully read");
        return new ArrayList<>(storage.getTrainingTypes().values());
    }
}
