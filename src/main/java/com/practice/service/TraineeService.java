package com.practice.service;


import com.practice.dao.TraineeRepository;
import com.practice.dao.TrainerRepository;
import com.practice.dto.request.*;
import com.practice.dto.response.GetTraineeProfileResponse;
import com.practice.dto.response.RegistrationResponse;
import com.practice.dto.response.TrainerSummaryResponse;
import com.practice.dto.response.UpdateTraineeProfileResponse;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.User;
import com.practice.exception.AuthenticationException;
import com.practice.mapper.request.TraineeRegistrationRequestMapper;
import com.practice.mapper.response.GetTraineeProfileResponseMapper;
import com.practice.mapper.response.TraineeRegistrationResponseMapper;
import com.practice.mapper.response.TrainerSummaryResponseMapper;
import com.practice.mapper.response.UpdateTraineeProfileResponseMapper;
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

@Service
@Transactional(readOnly = true)
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);
    private UserService userService;
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;

    private TraineeRegistrationRequestMapper traineeRegistrationRequestMapper;
    private TraineeRegistrationResponseMapper registrationResponseMapper;
    private UpdateTraineeProfileResponseMapper updateTraineeProfileResponseMapper;
    private GetTraineeProfileResponseMapper getTraineeProfileResponseMapper;
    private TrainerSummaryResponseMapper trainerSummaryResponseMapper;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setTraineeRegistrationRequestMapper(TraineeRegistrationRequestMapper traineeRegistrationRequestMapper) {
        this.traineeRegistrationRequestMapper = traineeRegistrationRequestMapper;
    }

    @Autowired
    public void setRegistrationResponseMapper(TraineeRegistrationResponseMapper registrationResponseMapper) {
        this.registrationResponseMapper = registrationResponseMapper;
    }

    @Autowired
    public void setUpdateTraineeProfileResponseMapper(UpdateTraineeProfileResponseMapper updateTraineeProfileResponseMapper) {
        this.updateTraineeProfileResponseMapper = updateTraineeProfileResponseMapper;
    }

    @Autowired
    public void setGetTraineeProfileResponseMapper(GetTraineeProfileResponseMapper getTraineeProfileResponseMapper) {
        this.getTraineeProfileResponseMapper = getTraineeProfileResponseMapper;
    }

    @Autowired
    public void setTrainerSummaryResponseMapper(TrainerSummaryResponseMapper trainerSummaryResponseMapper) {
        this.trainerSummaryResponseMapper = trainerSummaryResponseMapper;
    }

    @Transactional
    public RegistrationResponse create(TraineeRegistrationRequest request) {
        Trainee traineeCreate = traineeRegistrationRequestMapper.mapFrom(request);

        Credentials credentials = userService.generateCredentials(
                traineeCreate.getUser().getFirstName(),
                traineeCreate.getUser().getLastName()
        );

        traineeCreate.getUser().setUserName(credentials.username());
        traineeCreate.getUser().setPassword(credentials.password());
        traineeCreate.getUser().setIsActive(true);

        Trainee saved = traineeRepository.save(traineeCreate);
        log.info("Created trainee profile username={}", saved.getUser().getUserName());
        return registrationResponseMapper.mapFrom(saved);
    }

    @Transactional
    public UpdateTraineeProfileResponse update(UpdateTraineeProfileRequest request) {

        Trainee trainee = traineeRepository.findByUsername(request.userName())
                .orElseThrow(() -> new EntityNotFoundException("Trainee " + request.userName() + " not found"));

        trainee.getUser().setFirstName(request.firstName());
        trainee.getUser().setLastName(request.lastName());
        trainee.getUser().setIsActive(request.isActive());

        if (request.dateOfBirth() != null) {
            trainee.setDateOfBirth(request.dateOfBirth());
        }
        if (request.address() != null) {
            trainee.setAddress(request.address());
        }

        log.info("Updated trainee username={}", request.userName());
        return updateTraineeProfileResponseMapper.mapFrom(trainee);
    }

    @Transactional
    public void delete(UsernameRequest request) {
        if (!traineeRepository.delete(request.userName())) {
            throw new EntityNotFoundException(
                    "Trainee " + request.userName() + " not found");
        }

        log.info("Deleted trainee username={}", request.userName());
    }

    public Optional<Trainee> findById(Long id) {
        return traineeRepository.findById(id);
    }

    public GetTraineeProfileResponse findByUsername(UsernameRequest request) {
        return traineeRepository.findByUsername(request.userName())
                .map(getTraineeProfileResponseMapper::mapFrom)
                .orElseThrow(() -> new EntityNotFoundException("Trainee " + request.userName() + " not found"));
    }

    public List<Trainee> findAll() {
        return traineeRepository.findAll();
    }

    @Transactional
    public void switchActive(SwitchActiveFlagRequest request) {
        Trainee trainee = traineeRepository.findByUsername(request.userName())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Trainee " + request.userName() + " not found"));

        trainee.getUser().setIsActive(request.isActive());

        log.info(
                "Changed isActive={} for trainee username={}",
                request.isActive(),
                request.userName());
    }

    @Transactional
    public List<TrainerSummaryResponse> updateTrainersList(UpdateTraineeTrainerListRequest request) {
        Trainee trainee = traineeRepository.findByUsername(request.traineeUserName())
                .orElseThrow(() -> new EntityNotFoundException("Trainee " + request.traineeUserName() + " not found"));

        List<Trainer> newTrainers = request.trainersList().stream()
                        .map(trainerRequest -> trainerRepository.findByUsername(trainerRequest.userName())
                                .orElseThrow(() -> new EntityNotFoundException("Trainer " + trainerRequest.userName() + " not found")))
                        .toList();

        new ArrayList<>(trainee.getTrainers())
                .forEach(trainee::removeTrainer);
        newTrainers.forEach(trainee::addTrainer);

        log.info("Updated trainers list for trainee username={}", request.traineeUserName());
        return trainee.getTrainers().stream()
                .map(trainerSummaryResponseMapper::mapFrom)
                .toList();
    }

}
