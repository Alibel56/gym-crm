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
import com.practice.entity.TrainingType;
import com.practice.entity.User;
import com.practice.mapper.request.CreateTrainingRequestMapper;
import com.practice.mapper.request.GetTraineeTrainingsListRequestMapper;
import com.practice.mapper.request.GetTrainerTrainingsListRequestMapper;
import com.practice.mapper.response.GetTraineeTrainingsListResponseMapper;
import com.practice.mapper.response.GetTrainerTrainingsListResponseMapper;
import com.practice.metrics.GymMetrics;
import com.practice.utility.Criteria;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private CreateTrainingRequestMapper createTrainingRequestMapper;

    @Mock
    private GetTrainerTrainingsListRequestMapper
            getTrainerTrainingsListRequestMapper;

    @Mock
    private GetTraineeTrainingsListRequestMapper
            getTraineeTrainingsListRequestMapper;

    @Mock
    private GetTraineeTrainingsListResponseMapper
            getTraineeTrainingsListResponseMapper;

    @Mock
    private GetTrainerTrainingsListResponseMapper
            getTrainerTrainingsListResponseMapper;

    @Mock
    private GymMetrics gymMetrics;

    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingService();

        trainingService.setTrainingRepository(trainingRepository);
        trainingService.setTrainerRepository(trainerRepository);
        trainingService.setTraineeRepository(traineeRepository);
        trainingService.setGymMetrics(gymMetrics);
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

    private Trainer createTrainer() {
        User user = User.builder()
                .userName("trainer.username")
                .firstName("Trainer")
                .lastName("Test")
                .password("password")
                .isActive(true)
                .build();

        TrainingType trainingType = TrainingType.builder()
                .id(1L)
                .name("Fitness")
                .build();

        return Trainer.builder()
                .id(1L)
                .user(user)
                .trainingType(trainingType)
                .build();
    }

    private Trainee createTrainee() {
        User user = User.builder()
                .userName("trainee.username")
                .firstName("Trainee")
                .lastName("Test")
                .password("password")
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(1L)
                .user(user)
                .build();
    }

    private Training createTraining() {
        return Training.builder()
                .id(1L)
                .build();
    }

    @Test
    void create_shouldCreateTraining() {

        CreateTrainingRequest request =
                mock(CreateTrainingRequest.class);

        Training training = createTraining();
        Trainer trainer = createTrainer();
        Trainee trainee = createTrainee();

        when(request.trainerUserName())
                .thenReturn("trainer.username");

        when(request.traineeUserName())
                .thenReturn("trainee.username");

        when(createTrainingRequestMapper.mapFrom(request))
                .thenReturn(training);

        when(trainerRepository.findByUsername("trainer.username"))
                .thenReturn(Optional.of(trainer));

        when(traineeRepository.findByUsername("trainee.username"))
                .thenReturn(Optional.of(trainee));

        when(trainingRepository.save(training))
                .thenReturn(training);


        trainingService.create(request);


        assertThat(training.getTrainer())
                .isSameAs(trainer);

        assertThat(training.getTrainee())
                .isSameAs(trainee);

        assertThat(training.getTrainingType())
                .isSameAs(trainer.getTrainingType());

        verify(createTrainingRequestMapper)
                .mapFrom(request);

        verify(trainerRepository)
                .findByUsername("trainer.username");

        verify(traineeRepository)
                .findByUsername("trainee.username");

        verify(trainingRepository)
                .save(training);

        verify(gymMetrics).startTrainingCreationTimer();
        verify(gymMetrics).stopTrainingCreationTimer(any());
    }

    @Test
    void create_shouldThrowWhenTrainerNotFound() {

        CreateTrainingRequest request =
                mock(CreateTrainingRequest.class);

        Training training = createTraining();

        when(request.trainerUserName())
                .thenReturn("unknown.trainer");

        when(createTrainingRequestMapper.mapFrom(request))
                .thenReturn(training);

        when(trainerRepository.findByUsername("unknown.trainer"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                trainingService.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(
                        "Trainer's username in new training not found"
                );

        verify(trainerRepository)
                .findByUsername("unknown.trainer");

        verifyNoInteractions(traineeRepository);

        verify(trainingRepository, never())
                .save(any());
    }

    @Test
    void create_shouldThrowWhenTraineeNotFound() {

        CreateTrainingRequest request =
                mock(CreateTrainingRequest.class);

        Training training = createTraining();
        Trainer trainer = createTrainer();

        when(request.trainerUserName())
                .thenReturn("trainer.username");

        when(request.traineeUserName())
                .thenReturn("unknown.trainee");

        when(createTrainingRequestMapper.mapFrom(request))
                .thenReturn(training);

        when(trainerRepository.findByUsername("trainer.username"))
                .thenReturn(Optional.of(trainer));

        when(traineeRepository.findByUsername("unknown.trainee"))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() ->
                trainingService.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(
                        "Trainee's username in new training not found"
                );

        assertThat(training.getTrainer())
                .isSameAs(trainer);

        assertThat(training.getTrainingType())
                .isSameAs(trainer.getTrainingType());

        verify(trainingRepository, never())
                .save(any());
    }

    @Test
    void findById_shouldReturnTraining() {

        Training training = createTraining();

        when(trainingRepository.findById(1L))
                .thenReturn(Optional.of(training));


        Optional<Training> result =
                trainingService.findById(1L);


        assertThat(result)
                .isPresent()
                .contains(training);

        verify(trainingRepository)
                .findById(1L);
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenTrainingNotFound() {

        when(trainingRepository.findById(100L))
                .thenReturn(Optional.empty());


        Optional<Training> result =
                trainingService.findById(100L);


        assertThat(result).isEmpty();

        verify(trainingRepository)
                .findById(100L);
    }

    @Test
    void findAll_shouldReturnAllTrainings() {

        Training firstTraining = Training.builder()
                .id(1L)
                .build();

        Training secondTraining = Training.builder()
                .id(2L)
                .build();

        when(trainingRepository.findAll())
                .thenReturn(List.of(
                        firstTraining,
                        secondTraining
                ));


        List<Training> result =
                trainingService.findAll();


        assertThat(result)
                .containsExactly(
                        firstTraining,
                        secondTraining
                );

        verify(trainingRepository)
                .findAll();
    }

    @Test
    void findAllTraineeTrainings_shouldReturnMappedResponses() {

        GetTraineeTrainingsListRequest request =
                mock(GetTraineeTrainingsListRequest.class);

        Criteria criteria = mock(Criteria.class);

        Training firstTraining = Training.builder()
                .id(1L)
                .build();

        Training secondTraining = Training.builder()
                .id(2L)
                .build();

        GetTraineeTrainingsListResponse firstResponse =
                mock(GetTraineeTrainingsListResponse.class);

        GetTraineeTrainingsListResponse secondResponse =
                mock(GetTraineeTrainingsListResponse.class);

        when(request.userName())
                .thenReturn("trainee.username");

        when(getTraineeTrainingsListRequestMapper.mapFrom(request))
                .thenReturn(criteria);

        when(trainingRepository.findAllTraineeTrainingsByCriteria(
                "trainee.username",
                criteria
        )).thenReturn(List.of(
                firstTraining,
                secondTraining
        ));

        when(getTraineeTrainingsListResponseMapper.mapFrom(firstTraining))
                .thenReturn(firstResponse);

        when(getTraineeTrainingsListResponseMapper.mapFrom(secondTraining))
                .thenReturn(secondResponse);


        List<GetTraineeTrainingsListResponse> result =
                trainingService.findAllTraineeTrainings(request);


        assertThat(result)
                .containsExactly(
                        firstResponse,
                        secondResponse
                );

        verify(getTraineeTrainingsListRequestMapper)
                .mapFrom(request);

        verify(trainingRepository)
                .findAllTraineeTrainingsByCriteria(
                        "trainee.username",
                        criteria
                );

        verify(getTraineeTrainingsListResponseMapper)
                .mapFrom(firstTraining);

        verify(getTraineeTrainingsListResponseMapper)
                .mapFrom(secondTraining);
    }

    @Test
    void findAllTraineeTrainings_shouldReturnEmptyList() {

        GetTraineeTrainingsListRequest request =
                mock(GetTraineeTrainingsListRequest.class);

        Criteria criteria = mock(Criteria.class);

        when(request.userName())
                .thenReturn("trainee.username");

        when(getTraineeTrainingsListRequestMapper.mapFrom(request))
                .thenReturn(criteria);

        when(trainingRepository.findAllTraineeTrainingsByCriteria(
                "trainee.username",
                criteria
        )).thenReturn(List.of());


        List<GetTraineeTrainingsListResponse> result =
                trainingService.findAllTraineeTrainings(request);


        assertThat(result).isEmpty();

        verifyNoInteractions(
                getTraineeTrainingsListResponseMapper
        );
    }

    @Test
    void findAllTrainerTrainings_shouldReturnMappedResponses() {

        GetTrainerTrainingsListRequest request =
                mock(GetTrainerTrainingsListRequest.class);

        Criteria criteria = mock(Criteria.class);

        Training firstTraining = Training.builder()
                .id(1L)
                .build();

        Training secondTraining = Training.builder()
                .id(2L)
                .build();

        GetTrainerTrainingsListResponse firstResponse =
                mock(GetTrainerTrainingsListResponse.class);

        GetTrainerTrainingsListResponse secondResponse =
                mock(GetTrainerTrainingsListResponse.class);

        when(request.userName())
                .thenReturn("trainer.username");

        when(getTrainerTrainingsListRequestMapper.mapFrom(request))
                .thenReturn(criteria);

        when(trainingRepository.findAllTrainerTrainingsByCriteria(
                "trainer.username",
                criteria
        )).thenReturn(List.of(
                firstTraining,
                secondTraining
        ));

        when(getTrainerTrainingsListResponseMapper.mapFrom(firstTraining))
                .thenReturn(firstResponse);

        when(getTrainerTrainingsListResponseMapper.mapFrom(secondTraining))
                .thenReturn(secondResponse);


        List<GetTrainerTrainingsListResponse> result =
                trainingService.findAllTrainerTrainings(request);


        assertThat(result)
                .containsExactly(
                        firstResponse,
                        secondResponse
                );

        verify(getTrainerTrainingsListRequestMapper)
                .mapFrom(request);

        verify(trainingRepository)
                .findAllTrainerTrainingsByCriteria(
                        "trainer.username",
                        criteria
                );

        verify(getTrainerTrainingsListResponseMapper)
                .mapFrom(firstTraining);

        verify(getTrainerTrainingsListResponseMapper)
                .mapFrom(secondTraining);
    }

    @Test
    void findAllTrainerTrainings_shouldReturnEmptyList() {

        GetTrainerTrainingsListRequest request =
                mock(GetTrainerTrainingsListRequest.class);

        Criteria criteria = mock(Criteria.class);

        when(request.userName())
                .thenReturn("trainer.username");

        when(getTrainerTrainingsListRequestMapper.mapFrom(request))
                .thenReturn(criteria);

        when(trainingRepository.findAllTrainerTrainingsByCriteria(
                "trainer.username",
                criteria
        )).thenReturn(List.of());


        List<GetTrainerTrainingsListResponse> result =
                trainingService.findAllTrainerTrainings(request);


        assertThat(result).isEmpty();

        verifyNoInteractions(
                getTrainerTrainingsListResponseMapper
        );
    }
}