package com.practice.service;

import com.practice.dao.TraineeRepositoryImpl;
import com.practice.dao.TrainerRepositoryImpl;
import com.practice.dao.TrainingTypeRepository;
import com.practice.dto.request.*;
import com.practice.dto.response.GetTrainerProfileResponse;
import com.practice.dto.response.RegistrationResponse;
import com.practice.dto.response.TrainerSummaryResponse;
import com.practice.dto.response.UpdateTrainerProfileResponse;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.User;
import com.practice.mapper.request.TrainerRegistrationRequestMapper;
import com.practice.mapper.response.GetTrainerProfileResponseMapper;
import com.practice.mapper.response.TrainerRegistrationResponseMapper;
import com.practice.mapper.response.TrainerSummaryResponseMapper;
import com.practice.mapper.response.UpdateTrainerProfileResponseMapper;
import com.practice.metrics.GymMetrics;
import com.practice.utility.Credentials;
import com.practice.utility.ServiceUtility;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private TrainerRepositoryImpl trainerRepository;
    private TraineeRepositoryImpl traineeRepository;
    private TrainingTypeRepository trainingTypeRepository;
    private UserService userService;
    private GymMetrics gymMetrics;

    private TrainerRegistrationRequestMapper trainerRegistrationRequestMapper;
    private TrainerRegistrationResponseMapper registrationResponseMapper;
    private UpdateTrainerProfileResponseMapper updateTrainerProfileResponseMapper;
    private GetTrainerProfileResponseMapper getTrainerProfileResponseMapper;
    private TrainerSummaryResponseMapper trainerSummaryResponseMapper;

    @Autowired
    public void setTrainerRepository(TrainerRepositoryImpl trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setTraineeRepository(TraineeRepositoryImpl traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setGymMetrics(GymMetrics gymMetrics) {
        this.gymMetrics = gymMetrics;
    }

    @Autowired
    public void setTrainerRegistrationRequestMapper(TrainerRegistrationRequestMapper trainerRegistrationRequestMapper) {
        this.trainerRegistrationRequestMapper = trainerRegistrationRequestMapper;
    }

    @Autowired
    public void setRegistrationResponseMapper(TrainerRegistrationResponseMapper registrationResponseMapper) {
        this.registrationResponseMapper = registrationResponseMapper;
    }

    @Autowired
    public void setUpdateTrainerProfileResponseMapper(UpdateTrainerProfileResponseMapper updateTrainerProfileResponseMapper) {
        this.updateTrainerProfileResponseMapper = updateTrainerProfileResponseMapper;
    }

    @Autowired
    public void setGetTrainerProfileResponseMapper(GetTrainerProfileResponseMapper getTrainerProfileResponseMapper) {
        this.getTrainerProfileResponseMapper = getTrainerProfileResponseMapper;
    }

    @Autowired
    public void setTrainerSummaryResponseMapper(TrainerSummaryResponseMapper trainerSummaryResponseMapper) {
        this.trainerSummaryResponseMapper = trainerSummaryResponseMapper;
    }

    @Transactional
    public RegistrationResponse create(TrainerRegistrationRequest request) {
        Trainer trainerCreate = trainerRegistrationRequestMapper.mapFrom(request);
        var trainingType = trainingTypeRepository.findById(request.specializationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "TrainingType with id " + request.specializationId() + " does not exist"
                ));

        trainerCreate.setTrainingType(trainingType);

        Credentials credentials = userService.generateCredentials(
                trainerCreate.getUser().getFirstName(),
                trainerCreate.getUser().getLastName()
        );

        trainerCreate.getUser().setUserName(credentials.username());
        trainerCreate.getUser().setPassword(credentials.password());
        trainerCreate.getUser().setIsActive(true);

        Trainer saved = trainerRepository.save(trainerCreate);
        gymMetrics.recordRegistration("trainer");
        log.info("Created trainer profile username={}", credentials.username());
        return registrationResponseMapper.mapFrom(saved);
    }

    @Transactional
    public UpdateTrainerProfileResponse update(UpdateTrainerProfileRequest request) {

        Trainer trainer = trainerRepository.findByUsername(request.userName())
                .orElseThrow(() -> new EntityNotFoundException("Trainer " + request.userName() + " not found"));

        trainer.getUser().setUserName(request.userName());
        trainer.getUser().setFirstName(request.firstName());
        trainer.getUser().setLastName(request.lastName());
        trainer.getUser().setIsActive(request.isActive());

        log.info("Updated trainer username={}", request.userName());
        return updateTrainerProfileResponseMapper.mapFrom(trainer);
    }

    public Optional<Trainer> findById(Long id) {
        return trainerRepository.findById(id);
    }

    public List<Trainer> findAll(){
        return trainerRepository.findAll();
    }

    public GetTrainerProfileResponse findByUsername(UsernameRequest request) {
        return trainerRepository.findByUsername(request.userName())
                .map(getTrainerProfileResponseMapper::mapFrom)
                .orElseThrow(() -> new EntityNotFoundException("Trainer " + request.userName() + " not found"));
    }

    public List<TrainerSummaryResponse> findAllNotAssignedByTraineeUserName(UsernameRequest request) {
        return trainerRepository.findAllNotAssignedByTraineeUserName(request.userName()).stream()
                .map(trainer -> trainerSummaryResponseMapper.mapFrom(trainer))
                .collect(Collectors.toList());
    }

    @Transactional
    public void switchActive(SwitchActiveFlagRequest request) {
        Trainer trainer = trainerRepository.findByUsername(request.userName())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Trainer " + request.userName() + " not found"));

        trainer.getUser().setIsActive(request.isActive());

        log.info(
                "Changed isActive={} for trainer username={}",
                request.isActive(),
                request.userName());
    }
}
