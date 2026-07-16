package com.practice.service;

import com.practice.dao.TraineeRepository;
import com.practice.dao.TrainerRepository;
import com.practice.dao.TrainingRepository;
import com.practice.dto.request.CreateTrainingRequest;
import com.practice.dto.request.GetTraineeTrainingsListRequest;
import com.practice.dto.request.GetTrainerTrainingsListRequest;
import com.practice.dto.response.GetTraineeTrainingsListResponse;
import com.practice.dto.response.GetTrainerTrainingsListResponse;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.mapper.request.CreateTrainingRequestMapper;
import com.practice.mapper.request.GetTraineeTrainingsListRequestMapper;
import com.practice.mapper.request.GetTrainerTrainingsListRequestMapper;
import com.practice.mapper.response.GetTraineeTrainingsListResponseMapper;
import com.practice.mapper.response.GetTrainerTrainingsListResponseMapper;
import com.practice.utility.Criteria;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private TrainingRepository trainingRepository;
    private TrainerRepository trainerRepository;
    private TraineeRepository traineeRepository;

    private CreateTrainingRequestMapper createTrainingRequestMapper;
    private GetTrainerTrainingsListRequestMapper getTrainerTrainingsListRequestMapper;
    private GetTraineeTrainingsListRequestMapper getTraineeTrainingsListRequestMapper;
    private GetTraineeTrainingsListResponseMapper getTraineeTrainingsListResponseMapper;
    private GetTrainerTrainingsListResponseMapper getTrainerTrainingsListResponseMapper;


    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setCreateTrainingRequestMapper(CreateTrainingRequestMapper createTrainingRequestMapper) {
        this.createTrainingRequestMapper = createTrainingRequestMapper;
    }

    @Autowired
    public void setGetTrainerTrainingsListRequestMapper(GetTrainerTrainingsListRequestMapper getTrainerTrainingsListRequestMapper) {
        this.getTrainerTrainingsListRequestMapper = getTrainerTrainingsListRequestMapper;
    }

    @Autowired
    public void setGetTraineeTrainingsListRequestMapper(GetTraineeTrainingsListRequestMapper getTraineeTrainingsListRequestMapper) {
        this.getTraineeTrainingsListRequestMapper = getTraineeTrainingsListRequestMapper;
    }

    @Autowired
    public void setGetTraineeTrainingsListResponseMapper(GetTraineeTrainingsListResponseMapper getTraineeTrainingsListResponseMapper) {
        this.getTraineeTrainingsListResponseMapper = getTraineeTrainingsListResponseMapper;
    }

    @Autowired
    public void setGetTrainerTrainingsListResponseMapper(GetTrainerTrainingsListResponseMapper getTrainerTrainingsListResponseMapper) {
        this.getTrainerTrainingsListResponseMapper = getTrainerTrainingsListResponseMapper;
    }

    @Transactional
    public void create(CreateTrainingRequest request) {

        Training trainingCreate = createTrainingRequestMapper.mapFrom(request);

        Trainer trainer = trainerRepository.findByUsername(request.trainerUserName())
                .orElseThrow(() -> new EntityNotFoundException("Trainer's username in new training not found"));

        trainingCreate.setTrainer(trainer);
        trainingCreate.setTrainingType(trainer.getTrainingType());

        Trainee trainee = traineeRepository.findByUsername(request.traineeUserName())
                .orElseThrow(() -> new EntityNotFoundException("Trainee's username in new training not found"));

        trainingCreate.setTrainee(trainee);

        Training saved = trainingRepository.save(trainingCreate);
        log.info("Created training trainee={} trainer={}", request.traineeUserName(), request.trainerUserName());
    }

    public Optional<Training> findById(Long id) {
        return trainingRepository.findById(id);
    }

    public List<Training> findAll() {
        return trainingRepository.findAll();
    }

    public List<GetTraineeTrainingsListResponse> findAllTraineeTrainings(GetTraineeTrainingsListRequest request) {
        Criteria criteria = getTraineeTrainingsListRequestMapper.mapFrom(request);
        return trainingRepository.findAllTraineeTrainingsByCriteria(request.userName(), criteria).stream()
                .map(getTraineeTrainingsListResponseMapper::mapFrom)
                .collect(Collectors.toList());
    }

    public List<GetTrainerTrainingsListResponse> findAllTrainerTrainings(GetTrainerTrainingsListRequest request) {
        Criteria criteria = getTrainerTrainingsListRequestMapper.mapFrom(request);
        return trainingRepository.findAllTrainerTrainingsByCriteria(request.userName(), criteria).stream()
                .map(getTrainerTrainingsListResponseMapper::mapFrom)
                .collect(Collectors.toList());
    }

}
