package com.practice.facade;

import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.service.TraineeService;
import com.practice.service.TrainerService;
import com.practice.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public Long createTrainee(Trainee traineeCreate){
        return traineeService.create(traineeCreate);
    }

    public boolean updateTrainee(Trainee traineeUpdate){
        return traineeService.update(traineeUpdate);
    }

    public boolean deleteTrainee(Long id){
        return traineeService.delete(id);
    }

    public Optional<Trainee> findByIdTrainee(Long id){
        return traineeService.findById(id);
    }

    public List<Trainee> findAllTrainee(){
        return traineeService.findAll();
    }

    public Long createTrainer(Trainer trainerCreate){
        return trainerService.create(trainerCreate);
    }

    public boolean updateTrainer(Trainer trainerUpdate){
        return trainerService.update(trainerUpdate);
    }

    public Optional<Trainer> findByIdTrainer(Long id){
        return trainerService.findById(id);
    }

    public List<Trainer> findAllTrainer(){
        return trainerService.findAll();
    }

    public Long createTraining(Training trainingCreate){
        return trainingService.create(trainingCreate);
    }

    public Optional<Training> findByIdTraining(Long id){
        return trainingService.findById(id);
    }

    public List<Training> findAllTraining(){
        return trainingService.findAll();
    }
}
