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
import com.practice.entity.User;
import com.practice.mapper.request.CreateTrainingRequestMapper;
import com.practice.mapper.request.GetTraineeTrainingsListRequestMapper;
import com.practice.mapper.request.GetTrainerTrainingsListRequestMapper;
import com.practice.mapper.response.GetTraineeTrainingsListResponseMapper;
import com.practice.mapper.response.GetTrainerTrainingsListResponseMapper;
import com.practice.messaging.TrainingWorkloadEventPublisher;
import com.practice.metrics.GymMetrics;
import com.practice.utility.Criteria;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private GymMetrics gymMetrics;

    @Mock
    private TrainingWorkloadEventPublisher workloadEventPublisher;

    @Mock
    private CreateTrainingRequestMapper createTrainingRequestMapper;

    @Mock
    private GetTrainerTrainingsListRequestMapper getTrainerTrainingsListRequestMapper;

    @Mock
    private GetTraineeTrainingsListRequestMapper getTraineeTrainingsListRequestMapper;

    @Mock
    private GetTraineeTrainingsListResponseMapper getTraineeTrainingsListResponseMapper;

    @Mock
    private GetTrainerTrainingsListResponseMapper getTrainerTrainingsListResponseMapper;

    @Mock
    private Timer.Sample timerSample;

    private TrainingService trainingService;

    @BeforeEach
    void setUp() {

        trainingService = new TrainingService();

        trainingService.setTrainingRepository(trainingRepository);
        trainingService.setTrainerRepository(trainerRepository);
        trainingService.setTraineeRepository(traineeRepository);
        trainingService.setGymMetrics(gymMetrics);
        trainingService.setWorkloadEventPublisher(workloadEventPublisher);

        trainingService.setCreateTrainingRequestMapper(
                createTrainingRequestMapper
        );

        trainingService.setGetTrainerTrainingsListRequestMapper(
                getTrainerTrainingsListRequestMapper
        );

        trainingService.setGetTraineeTrainingsListRequestMapper(
                getTraineeTrainingsListRequestMapper
        );

        trainingService.setGetTraineeTrainingsListResponseMapper(
                getTraineeTrainingsListResponseMapper
        );

        trainingService.setGetTrainerTrainingsListResponseMapper(
                getTrainerTrainingsListResponseMapper
        );
    }

    @Test
    void findById_shouldReturnTraining() {

        Training training = mock(Training.class);

        when(trainingRepository.findById(1L))
                .thenReturn(Optional.of(training));

        Optional<Training> result =
                trainingService.findById(1L);

        assertTrue(result.isPresent());
        assertSame(training, result.get());

        verify(trainingRepository)
                .findById(1L);
    }


    @Test
    void findAll_shouldReturnTrainings() {

        Training training1 = mock(Training.class);
        Training training2 = mock(Training.class);

        when(trainingRepository.findAll())
                .thenReturn(List.of(training1, training2));

        List<Training> result =
                trainingService.findAll();

        assertEquals(2, result.size());
        assertSame(training1, result.get(0));
        assertSame(training2, result.get(1));

        verify(trainingRepository)
                .findAll();
    }

    @Test
    void create_shouldCreateTraining() {

        CreateTrainingRequest request =
                mock(CreateTrainingRequest.class);

        Training training =
                mock(Training.class);

        Trainer trainer =
                mock(Trainer.class);

        Trainee trainee =
                mock(Trainee.class);

        User user =
                mock(User.class);

        when(request.trainerUserName())
                .thenReturn("trainer");

        when(request.traineeUserName())
                .thenReturn("trainee");

        when(createTrainingRequestMapper.mapFrom(request))
                .thenReturn(training);

        when(trainerRepository.findByUsername("trainer"))
                .thenReturn(Optional.of(trainer));

        when(traineeRepository.findByUsername("trainee"))
                .thenReturn(Optional.of(trainee));

        when(trainingRepository.save(training))
                .thenReturn(training);

        // Training -> Trainer -> User
        when(training.getTrainer())
                .thenReturn(trainer);

        when(trainer.getUser())
                .thenReturn(user);

        when(user.getUserName())
                .thenReturn("trainer");

        when(user.getFirstName())
                .thenReturn("John");

        when(user.getLastName())
                .thenReturn("Doe");

        when(user.getIsActive())
                .thenReturn(true);

        when(gymMetrics.startTrainingCreationTimer())
                .thenReturn(timerSample);

        trainingService.create(request);

        verify(createTrainingRequestMapper)
                .mapFrom(request);

        verify(trainerRepository)
                .findByUsername("trainer");

        verify(traineeRepository)
                .findByUsername("trainee");

        verify(training)
                .setTrainer(trainer);

        verify(training)
                .setTrainee(trainee);

        verify(trainingRepository)
                .save(training);

        verify(workloadEventPublisher)
                .publish(any());

        verify(gymMetrics)
                .startTrainingCreationTimer();

        verify(gymMetrics)
                .stopTrainingCreationTimer(timerSample);
    }

    @Test
    void create_shouldThrowExceptionWhenTrainerNotFound() {

        CreateTrainingRequest request =
                mock(CreateTrainingRequest.class);

        Training training =
                mock(Training.class);

        when(request.trainerUserName())
                .thenReturn("trainer");

        when(createTrainingRequestMapper.mapFrom(request))
                .thenReturn(training);

        when(trainerRepository.findByUsername("trainer"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> trainingService.create(request)
        );

        verify(trainingRepository, never())
                .save(any());

        verify(traineeRepository, never())
                .findByUsername(anyString());
    }

    @Test
    void create_shouldThrowExceptionWhenTraineeNotFound() {

        CreateTrainingRequest request =
                mock(CreateTrainingRequest.class);

        Training training =
                mock(Training.class);

        Trainer trainer =
                mock(Trainer.class);

        when(request.trainerUserName())
                .thenReturn("trainer");

        when(request.traineeUserName())
                .thenReturn("trainee");

        when(createTrainingRequestMapper.mapFrom(request))
                .thenReturn(training);

        when(trainerRepository.findByUsername("trainer"))
                .thenReturn(Optional.of(trainer));

        when(traineeRepository.findByUsername("trainee"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> trainingService.create(request)
        );

        verify(trainingRepository, never())
                .save(any());
    }


    @Test
    void delete_shouldDeleteTraining() {

        Training training = mock(Training.class);

        Trainer trainer = mock(Trainer.class);
        Trainee trainee = mock(Trainee.class);

        User trainerUser = mock(User.class);
        User traineeUser = mock(User.class);

        when(trainingRepository.findById(1L))
                .thenReturn(Optional.of(training));

        when(training.getTrainer())
                .thenReturn(trainer);

        when(trainer.getUser())
                .thenReturn(trainerUser);

        when(trainerUser.getUserName())
                .thenReturn("trainer");

        when(trainerUser.getFirstName())
                .thenReturn("John");

        when(trainerUser.getLastName())
                .thenReturn("Doe");

        when(trainerUser.getIsActive())
                .thenReturn(true);

        // Training -> Trainee -> User
        when(training.getTrainee())
                .thenReturn(trainee);

        when(trainee.getUser())
                .thenReturn(traineeUser);

        when(traineeUser.getUserName())
                .thenReturn("trainee");

        trainingService.delete(1L);

        verify(trainingRepository)
                .findById(1L);

        verify(trainingRepository)
                .delete(training);

        verify(workloadEventPublisher)
                .publish(any());
    }

    @Test
    void delete_shouldThrowExceptionWhenTrainingNotFound() {

        when(trainingRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> trainingService.delete(1L)
        );

        verify(trainingRepository, never())
                .delete(any());

        verify(workloadEventPublisher, never())
                .publish(any());
    }

    @Test
    void findAllTraineeTrainings_shouldReturnResponses() {

        GetTraineeTrainingsListRequest request =
                mock(GetTraineeTrainingsListRequest.class);

        Criteria criteria =
                mock(Criteria.class);

        Training training =
                mock(Training.class);

        GetTraineeTrainingsListResponse response =
                mock(GetTraineeTrainingsListResponse.class);

        when(request.userName())
                .thenReturn("trainee");

        when(getTraineeTrainingsListRequestMapper.mapFrom(request))
                .thenReturn(criteria);

        when(trainingRepository.findAllTraineeTrainingsByCriteria(
                "trainee",
                criteria
        )).thenReturn(List.of(training));

        when(getTraineeTrainingsListResponseMapper.mapFrom(training))
                .thenReturn(response);

        List<GetTraineeTrainingsListResponse> result =
                trainingService.findAllTraineeTrainings(request);

        assertEquals(1, result.size());
        assertSame(response, result.getFirst());

        verify(trainingRepository)
                .findAllTraineeTrainingsByCriteria(
                        "trainee",
                        criteria
                );

        verify(getTraineeTrainingsListResponseMapper)
                .mapFrom(training);
    }


    @Test
    void findAllTrainerTrainings_shouldReturnResponses() {

        GetTrainerTrainingsListRequest request =
                mock(GetTrainerTrainingsListRequest.class);

        Criteria criteria =
                mock(Criteria.class);

        Training training =
                mock(Training.class);

        GetTrainerTrainingsListResponse response =
                mock(GetTrainerTrainingsListResponse.class);

        when(request.userName())
                .thenReturn("trainer");

        when(getTrainerTrainingsListRequestMapper.mapFrom(request))
                .thenReturn(criteria);

        when(trainingRepository.findAllTrainerTrainingsByCriteria(
                "trainer",
                criteria
        )).thenReturn(List.of(training));

        when(getTrainerTrainingsListResponseMapper.mapFrom(training))
                .thenReturn(response);

        List<GetTrainerTrainingsListResponse> result =
                trainingService.findAllTrainerTrainings(request);

        assertEquals(1, result.size());
        assertSame(response, result.getFirst());

        verify(trainingRepository)
                .findAllTrainerTrainingsByCriteria(
                        "trainer",
                        criteria
                );

        verify(getTrainerTrainingsListResponseMapper)
                .mapFrom(training);
    }
}