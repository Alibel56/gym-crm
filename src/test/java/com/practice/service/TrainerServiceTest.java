package com.practice.service;

import com.practice.dao.TraineeRepositoryImpl;
import com.practice.dao.TrainerRepositoryImpl;
import com.practice.dao.TrainingTypeRepository;
import com.practice.dto.request.SwitchActiveFlagRequest;
import com.practice.dto.request.TrainerRegistrationRequest;
import com.practice.dto.request.UpdateTrainerProfileRequest;
import com.practice.dto.request.UsernameRequest;
import com.practice.dto.response.GetTrainerProfileResponse;
import com.practice.dto.response.RegistrationResponse;
import com.practice.dto.response.TrainerSummaryResponse;
import com.practice.dto.response.UpdateTrainerProfileResponse;
import com.practice.entity.Trainer;
import com.practice.entity.TrainingType;
import com.practice.entity.User;
import com.practice.mapper.request.TrainerRegistrationRequestMapper;
import com.practice.mapper.response.GetTrainerProfileResponseMapper;
import com.practice.mapper.response.TrainerSummaryResponseMapper;
import com.practice.mapper.response.UpdateTrainerProfileResponseMapper;
import com.practice.metrics.GymMetrics;
import com.practice.testtags.ExceptionTest;
import com.practice.testtags.NegativeTest;
import com.practice.testtags.PositiveTest;
import com.practice.utility.Credentials;
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
class TrainerServiceTest {

    @Mock
    private TrainerRepositoryImpl trainerRepository;

    @Mock
    private TraineeRepositoryImpl traineeRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private UserService userService;

    @Mock
    private GymMetrics gymMetrics;

    @Mock
    private TrainerRegistrationRequestMapper registrationRequestMapper;

    @Mock
    private UpdateTrainerProfileResponseMapper updateResponseMapper;

    @Mock
    private GetTrainerProfileResponseMapper getProfileResponseMapper;

    @Mock
    private TrainerSummaryResponseMapper trainerSummaryResponseMapper;

    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerService();

        trainerService.setTrainerRepository(trainerRepository);
        trainerService.setTraineeRepository(traineeRepository);
        trainerService.setTrainingTypeRepository(trainingTypeRepository);
        trainerService.setUserService(userService);
        trainerService.setGymMetrics(gymMetrics);
        trainerService.setTrainerRegistrationRequestMapper(registrationRequestMapper);
        trainerService.setUpdateTrainerProfileResponseMapper(updateResponseMapper);
        trainerService.setGetTrainerProfileResponseMapper(getProfileResponseMapper);
        trainerService.setTrainerSummaryResponseMapper(trainerSummaryResponseMapper);
    }

    @Test
    @PositiveTest
    void createShouldCreateTrainerAndReturnCredentials() {
        TrainerRegistrationRequest request =
                mock(TrainerRegistrationRequest.class);

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Smith");

        Trainer trainer = new Trainer();
        trainer.setUser(user);

        TrainingType trainingType = new TrainingType();

        Credentials credentials =
                new Credentials("john.smith", "generatedPassword");

        when(request.specializationId()).thenReturn(1L);
        when(registrationRequestMapper.mapFrom(request)).thenReturn(trainer);
        when(trainingTypeRepository.findById(1L))
                .thenReturn(Optional.of(trainingType));
        when(userService.generateCredentials("John", "Smith"))
                .thenReturn(credentials);
        when(userService.encodePassword("generatedPassword"))
                .thenReturn("encodedPassword");
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        RegistrationResponse response = trainerService.create(request);

        assertThat(response.userName()).isEqualTo("john.smith");
        assertThat(response.password()).isEqualTo("generatedPassword");

        assertThat(trainer.getTrainingType()).isSameAs(trainingType);
        assertThat(user.getUserName()).isEqualTo("john.smith");
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(user.getIsActive()).isTrue();

        verify(trainerRepository).save(trainer);
        verify(gymMetrics).recordRegistration("trainer");
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void createShouldThrowExceptionWhenTrainingTypeNotFound() {
        TrainerRegistrationRequest request =
                mock(TrainerRegistrationRequest.class);

        Trainer trainer = new Trainer();
        trainer.setUser(new User());

        when(request.specializationId()).thenReturn(99L);
        when(registrationRequestMapper.mapFrom(request)).thenReturn(trainer);
        when(trainingTypeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("TrainingType with id 99 does not exist");

        verify(trainerRepository, never()).save(any());
        verifyNoInteractions(gymMetrics);
    }

    @Test
    @PositiveTest
    void updateShouldUpdateTrainerProfile() {
        UpdateTrainerProfileRequest request =
                mock(UpdateTrainerProfileRequest.class);

        User user = new User();

        Trainer trainer = new Trainer();
        trainer.setUser(user);

        UpdateTrainerProfileResponse expectedResponse =
                mock(UpdateTrainerProfileResponse.class);

        when(request.userName()).thenReturn("john");
        when(request.firstName()).thenReturn("Johnny");
        when(request.lastName()).thenReturn("Smith");
        when(request.isActive()).thenReturn(true);

        when(trainerRepository.findByUsername("john"))
                .thenReturn(Optional.of(trainer));
        when(updateResponseMapper.mapFrom(trainer))
                .thenReturn(expectedResponse);

        UpdateTrainerProfileResponse response =
                trainerService.update(request);

        assertThat(response).isSameAs(expectedResponse);
        assertThat(user.getUserName()).isEqualTo("john");
        assertThat(user.getFirstName()).isEqualTo("Johnny");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getIsActive()).isTrue();

        verify(updateResponseMapper).mapFrom(trainer);
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void updateShouldThrowExceptionWhenTrainerNotFound() {
        UpdateTrainerProfileRequest request =
                mock(UpdateTrainerProfileRequest.class);

        when(request.userName()).thenReturn("unknown");
        when(trainerRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.update(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainer unknown not found");
    }

    @Test
    @PositiveTest
    void findByIdShouldReturnTrainer() {
        Trainer trainer = new Trainer();

        when(trainerRepository.findById(1L))
                .thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.findById(1L);

        assertThat(result).contains(trainer);
    }

    @Test
    @PositiveTest
    void findAllShouldReturnAllTrainers() {
        List<Trainer> trainers =
                List.of(new Trainer(), new Trainer());

        when(trainerRepository.findAll()).thenReturn(trainers);

        List<Trainer> result = trainerService.findAll();

        assertThat(result).isEqualTo(trainers);
    }

    @Test
    @PositiveTest
    void findByUsernameShouldReturnMappedProfile() {
        UsernameRequest request = mock(UsernameRequest.class);
        Trainer trainer = new Trainer();

        GetTrainerProfileResponse expectedResponse =
                mock(GetTrainerProfileResponse.class);

        when(request.userName()).thenReturn("john");
        when(trainerRepository.findByUsername("john"))
                .thenReturn(Optional.of(trainer));
        when(getProfileResponseMapper.mapFrom(trainer))
                .thenReturn(expectedResponse);

        GetTrainerProfileResponse response =
                trainerService.findByUsername(request);

        assertThat(response).isSameAs(expectedResponse);

        verify(getProfileResponseMapper).mapFrom(trainer);
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void findByUsernameShouldThrowExceptionWhenTrainerNotFound() {
        UsernameRequest request = mock(UsernameRequest.class);

        when(request.userName()).thenReturn("unknown");
        when(trainerRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.findByUsername(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainer unknown not found");
    }

    @Test
    @PositiveTest
    void findAllNotAssignedShouldReturnMappedTrainers() {
        UsernameRequest request = mock(UsernameRequest.class);

        Trainer firstTrainer = new Trainer();
        Trainer secondTrainer = new Trainer();

        TrainerSummaryResponse firstResponse =
                mock(TrainerSummaryResponse.class);
        TrainerSummaryResponse secondResponse =
                mock(TrainerSummaryResponse.class);

        when(request.userName()).thenReturn("trainee");

        when(trainerRepository.findAllNotAssignedByTraineeUserName("trainee"))
                .thenReturn(List.of(firstTrainer, secondTrainer));

        when(trainerSummaryResponseMapper.mapFrom(same(firstTrainer)))
                .thenReturn(firstResponse);

        when(trainerSummaryResponseMapper.mapFrom(same(secondTrainer)))
                .thenReturn(secondResponse);

        List<TrainerSummaryResponse> result =
                trainerService.findAllNotAssignedByTraineeUserName(request);

        assertThat(result)
                .containsExactly(firstResponse, secondResponse);

        verify(trainerSummaryResponseMapper).mapFrom(same(firstTrainer));
        verify(trainerSummaryResponseMapper).mapFrom(same(secondTrainer));
    }

    @Test
    @PositiveTest
    void switchActiveShouldChangeActiveStatus() {
        SwitchActiveFlagRequest request =
                mock(SwitchActiveFlagRequest.class);

        User user = new User();
        user.setIsActive(false);

        Trainer trainer = new Trainer();
        trainer.setUser(user);

        when(request.userName()).thenReturn("john");
        when(request.isActive()).thenReturn(true);
        when(trainerRepository.findByUsername("john"))
                .thenReturn(Optional.of(trainer));

        trainerService.switchActive(request);

        assertThat(user.getIsActive()).isTrue();
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void switchActiveShouldThrowExceptionWhenTrainerNotFound() {
        SwitchActiveFlagRequest request =
                mock(SwitchActiveFlagRequest.class);

        when(request.userName()).thenReturn("unknown");
        when(trainerRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.switchActive(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainer unknown not found");
    }
}
