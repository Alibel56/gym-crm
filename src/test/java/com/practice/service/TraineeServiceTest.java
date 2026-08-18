package com.practice.service;

import com.practice.dao.TraineeRepository;
import com.practice.dao.TrainerRepository;
import com.practice.dto.request.*;
import com.practice.dto.response.*;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.User;
import com.practice.mapper.request.TraineeRegistrationRequestMapper;
import com.practice.mapper.response.*;
import com.practice.messaging.TrainingWorkloadEvent;
import com.practice.messaging.TrainingWorkloadEventPublisher;
import com.practice.messaging.WorkloadActionType;
import com.practice.metrics.GymMetrics;
import com.practice.utility.Credentials;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private GymMetrics gymMetrics;

    @Mock
    private TrainingWorkloadEventPublisher workloadEventPublisher;

    @Mock
    private TraineeRegistrationRequestMapper traineeRegistrationRequestMapper;

    @Mock
    private TraineeRegistrationResponseMapper registrationResponseMapper;

    @Mock
    private UpdateTraineeProfileResponseMapper updateTraineeProfileResponseMapper;

    @Mock
    private GetTraineeProfileResponseMapper getTraineeProfileResponseMapper;

    @Mock
    private TrainerSummaryResponseMapper trainerSummaryResponseMapper;

    @InjectMocks
    private TraineeService traineeService;


    @Test
    void create_shouldCreateTraineeAndReturnCredentials() {

        TraineeRegistrationRequest request = mock(TraineeRegistrationRequest.class);

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Smith");

        Trainee trainee = new Trainee();
        trainee.setUser(user);

        Credentials credentials =
                new Credentials("john.smith", "password123");

        when(traineeRegistrationRequestMapper.mapFrom(request))
                .thenReturn(trainee);

        when(userService.generateCredentials("John", "Smith"))
                .thenReturn(credentials);

        when(userService.encodePassword("password123"))
                .thenReturn("encodedPassword");

        when(traineeRepository.save(trainee))
                .thenReturn(trainee);

        RegistrationResponse result =
                traineeService.create(request);

        assertNotNull(result);
        assertEquals("john.smith", result.userName());
        assertEquals("password123", result.password());

        assertEquals("john.smith", user.getUserName());
        assertEquals("encodedPassword", user.getPassword());
        assertTrue(user.getIsActive());

        verify(traineeRepository).save(trainee);
        verify(gymMetrics).recordRegistration("trainee");
    }


    @Test
    void update_shouldUpdateTrainee() {

        UpdateTraineeProfileRequest request =
                mock(UpdateTraineeProfileRequest.class);

        when(request.userName()).thenReturn("john.smith");
        when(request.firstName()).thenReturn("John");
        when(request.lastName()).thenReturn("Updated");
        when(request.isActive()).thenReturn(true);
        when(request.dateOfBirth()).thenReturn(LocalDate.of(2000, 1, 1));
        when(request.address()).thenReturn("New address");

        User user = new User();
        user.setFirstName("Old");
        user.setLastName("Name");

        Trainee trainee = new Trainee();
        trainee.setUser(user);

        UpdateTraineeProfileResponse response =
                mock(UpdateTraineeProfileResponse.class);

        when(traineeRepository.findByUsername("john.smith"))
                .thenReturn(Optional.of(trainee));

        when(updateTraineeProfileResponseMapper.mapFrom(trainee))
                .thenReturn(response);

        UpdateTraineeProfileResponse result =
                traineeService.update(request);

        assertSame(response, result);

        assertEquals("John", user.getFirstName());
        assertEquals("Updated", user.getLastName());
        assertTrue(user.getIsActive());

        assertEquals(
                LocalDate.of(2000, 1, 1),
                trainee.getDateOfBirth()
        );

        assertEquals("New address", trainee.getAddress());

        verify(updateTraineeProfileResponseMapper)
                .mapFrom(trainee);
    }


    @Test
    void update_shouldThrowExceptionWhenTraineeNotFound() {

        UpdateTraineeProfileRequest request =
                mock(UpdateTraineeProfileRequest.class);

        when(request.userName()).thenReturn("unknown");

        when(traineeRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.update(request)
        );

        verifyNoInteractions(updateTraineeProfileResponseMapper);
    }


    @Test
    void findByUsername_shouldReturnProfile() {

        UsernameRequest request =
                new UsernameRequest("john.smith");

        Trainee trainee = new Trainee();

        GetTraineeProfileResponse response =
                mock(GetTraineeProfileResponse.class);

        when(traineeRepository.findByUsername("john.smith"))
                .thenReturn(Optional.of(trainee));

        when(getTraineeProfileResponseMapper.mapFrom(trainee))
                .thenReturn(response);

        GetTraineeProfileResponse result =
                traineeService.findByUsername(request);

        assertSame(response, result);

        verify(getTraineeProfileResponseMapper)
                .mapFrom(trainee);
    }


    @Test
    void findByUsername_shouldThrowExceptionWhenNotFound() {

        UsernameRequest request =
                new UsernameRequest("unknown");

        when(traineeRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.findByUsername(request)
        );
    }


    @Test
    void switchActive_shouldChangeActiveFlag() {

        SwitchActiveFlagRequest request =
                new SwitchActiveFlagRequest("john.smith", false);

        User user = new User();
        user.setIsActive(true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(traineeRepository.findByUsername("john.smith"))
                .thenReturn(Optional.of(trainee));

        traineeService.switchActive(request);

        assertFalse(user.getIsActive());

        verify(traineeRepository)
                .findByUsername("john.smith");
    }


    @Test
    void switchActive_shouldThrowExceptionWhenNotFound() {

        SwitchActiveFlagRequest request =
                new SwitchActiveFlagRequest("unknown", true);

        when(traineeRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.switchActive(request)
        );
    }


    @Test
    void delete_shouldDeleteTraineeAndPublishDeleteEvents() {

        UsernameRequest request =
                new UsernameRequest("john.smith");

        User trainerUser = new User();
        trainerUser.setUserName("trainer1");
        trainerUser.setFirstName("Mike");
        trainerUser.setLastName("Trainer");
        trainerUser.setIsActive(true);

        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);

        Training training = new Training();
        training.setTrainer(trainer);
        training.setTrainingDate(LocalDate.of(2026, 8, 18));
        training.setTrainingDuration(60);

        Trainee trainee = new Trainee();
        trainee.getTrainings().add(training);

        when(traineeRepository.findByUsername("john.smith"))
                .thenReturn(Optional.of(trainee));

        when(traineeRepository.delete("john.smith"))
                .thenReturn(true);

        traineeService.delete(request);

        verify(traineeRepository)
                .delete("john.smith");

        ArgumentCaptor<TrainingWorkloadEvent> captor =
                ArgumentCaptor.forClass(TrainingWorkloadEvent.class);

        verify(workloadEventPublisher)
                .publish(captor.capture());

        TrainingWorkloadEvent event = captor.getValue();

        assertEquals("trainer1", event.trainerUsername());
        assertEquals("Mike", event.trainerFirstName());
        assertEquals("Trainer", event.trainerLastName());
        assertTrue(event.isActive());
        assertEquals(
                LocalDate.of(2026, 8, 18),
                event.trainingDate()
        );
        assertEquals(60, event.trainingDuration());
        assertEquals(
                WorkloadActionType.DELETE,
                event.actionType()
        );
    }


    @Test
    void delete_shouldThrowExceptionWhenTraineeNotFound() {

        UsernameRequest request =
                new UsernameRequest("unknown");

        when(traineeRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.delete(request)
        );

        verify(traineeRepository, never())
                .delete(anyString());

        verifyNoInteractions(workloadEventPublisher);
    }


    @Test
    void delete_shouldThrowExceptionWhenRepositoryDeleteFails() {

        UsernameRequest request =
                new UsernameRequest("john.smith");

        Trainee trainee = new Trainee();

        when(traineeRepository.findByUsername("john.smith"))
                .thenReturn(Optional.of(trainee));

        when(traineeRepository.delete("john.smith"))
                .thenReturn(false);

        assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.delete(request)
        );

        verify(workloadEventPublisher, never())
                .publish(any());
    }


    @Test
    void findById_shouldReturnTrainee() {

        Trainee trainee = new Trainee();

        when(traineeRepository.findById(1L))
                .thenReturn(Optional.of(trainee));

        Optional<Trainee> result =
                traineeService.findById(1L);

        assertTrue(result.isPresent());
        assertSame(trainee, result.get());
    }


    @Test
    void findAll_shouldReturnAllTrainees() {

        List<Trainee> trainees =
                List.of(new Trainee(), new Trainee());

        when(traineeRepository.findAll())
                .thenReturn(trainees);

        List<Trainee> result =
                traineeService.findAll();

        assertEquals(2, result.size());
        assertSame(trainees, result);

        verify(traineeRepository).findAll();
    }


    @Test
    void updateTrainersList_shouldReplaceTrainers() {

        UpdateTraineeTrainerListRequest request =
                mock(UpdateTraineeTrainerListRequest.class);

        when(request.traineeUserName())
                .thenReturn("trainee1");

        UsernameRequest trainerRequest =
                mock(UsernameRequest.class);

        when(trainerRequest.userName())
                .thenReturn("trainer1");

        when(request.trainersList())
                .thenReturn(List.of(trainerRequest));

        Trainee trainee = new Trainee();

        Trainer newTrainer = new Trainer();

        when(traineeRepository.findByUsername("trainee1"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("trainer1"))
                .thenReturn(Optional.of(newTrainer));

        TrainerSummaryResponse response =
                mock(TrainerSummaryResponse.class);

        when(trainerSummaryResponseMapper.mapFrom(newTrainer))
                .thenReturn(response);

        List<TrainerSummaryResponse> result =
                traineeService.updateTrainersList(request);

        assertEquals(1, result.size());

        assertEquals(response, result.get(0));

        assertTrue(
                trainee.getTrainers().contains(newTrainer)
        );

        verify(traineeRepository)
                .findByUsername("trainee1");

        verify(trainerRepository)
                .findByUsername("trainer1");

        verify(trainerSummaryResponseMapper)
                .mapFrom(newTrainer);
    }

    @Test
    void updateTrainersList_shouldThrowWhenTraineeNotFound() {

        UpdateTraineeTrainerListRequest request =
                mock(UpdateTraineeTrainerListRequest.class);

        when(request.traineeUserName())
                .thenReturn("unknown");

        when(traineeRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.updateTrainersList(request)
        );

        verifyNoInteractions(trainerRepository);
    }


    @Test
    void updateTrainersList_shouldThrowWhenTrainerNotFound() {

        UpdateTraineeTrainerListRequest request =
                mock(UpdateTraineeTrainerListRequest.class);

        when(request.traineeUserName())
                .thenReturn("trainee1");

        UsernameRequest trainerRequest =
                mock(UsernameRequest.class);

        when(trainerRequest.userName())
                .thenReturn("unknownTrainer");

        when(request.trainersList())
                .thenReturn(List.of(trainerRequest));

        Trainee trainee = new Trainee();

        when(traineeRepository.findByUsername("trainee1"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("unknownTrainer"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.updateTrainersList(request)
        );
    }
}