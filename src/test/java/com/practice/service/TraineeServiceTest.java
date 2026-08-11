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
import com.practice.integration.workload.WorkloadClient;
import com.practice.mapper.request.TraineeRegistrationRequestMapper;
import com.practice.mapper.response.GetTraineeProfileResponseMapper;
import com.practice.mapper.response.TrainerSummaryResponseMapper;
import com.practice.mapper.response.UpdateTraineeProfileResponseMapper;
import com.practice.metrics.GymMetrics;
import com.practice.utility.Credentials;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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
    private WorkloadClient workloadClient;

    @Mock
    private TraineeRegistrationRequestMapper registrationRequestMapper;

    @Mock
    private UpdateTraineeProfileResponseMapper updateResponseMapper;

    @Mock
    private GetTraineeProfileResponseMapper getProfileResponseMapper;

    @Mock
    private TrainerSummaryResponseMapper trainerSummaryResponseMapper;

    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        traineeService = new TraineeService();

        traineeService.setUserService(userService);
        traineeService.setTraineeRepository(traineeRepository);
        traineeService.setTrainerRepository(trainerRepository);
        traineeService.setGymMetrics(gymMetrics);
        traineeService.setWorkloadClient(workloadClient);
        traineeService.setTraineeRegistrationRequestMapper(registrationRequestMapper);
        traineeService.setUpdateTraineeProfileResponseMapper(updateResponseMapper);
        traineeService.setGetTraineeProfileResponseMapper(getProfileResponseMapper);
        traineeService.setTrainerSummaryResponseMapper(trainerSummaryResponseMapper);
    }

    @Test
    void createShouldCreateTraineeAndReturnCredentials() {
        TraineeRegistrationRequest request =
                mock(TraineeRegistrationRequest.class);

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Smith");

        Trainee trainee = new Trainee();
        trainee.setUser(user);

        Credentials credentials =
                new Credentials("john.smith", "generatedPassword");

        when(registrationRequestMapper.mapFrom(request))
                .thenReturn(trainee);

        when(userService.generateCredentials("John", "Smith"))
                .thenReturn(credentials);

        when(userService.encodePassword("generatedPassword"))
                .thenReturn("encodedPassword");

        when(traineeRepository.save(trainee))
                .thenReturn(trainee);

        RegistrationResponse response =
                traineeService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.userName()).isEqualTo("john.smith");
        assertThat(response.password()).isEqualTo("generatedPassword");

        assertThat(user.getUserName()).isEqualTo("john.smith");
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(user.getIsActive()).isTrue();

        verify(traineeRepository).save(trainee);
        verify(gymMetrics).recordRegistration("trainee");
    }

    @Test
    void updateShouldUpdateTraineeProfile() {
        UpdateTraineeProfileRequest request =
                mock(UpdateTraineeProfileRequest.class);

        User user = new User();
        user.setUserName("john");

        Trainee trainee = new Trainee();
        trainee.setUser(user);

        UpdateTraineeProfileResponse expectedResponse =
                mock(UpdateTraineeProfileResponse.class);

        LocalDate dateOfBirth = LocalDate.of(2000, 1, 1);

        when(request.userName()).thenReturn("john");
        when(request.firstName()).thenReturn("Johnny");
        when(request.lastName()).thenReturn("Smith");
        when(request.isActive()).thenReturn(true);
        when(request.dateOfBirth()).thenReturn(dateOfBirth);
        when(request.address()).thenReturn("Almaty");

        when(traineeRepository.findByUsername("john"))
                .thenReturn(Optional.of(trainee));

        when(updateResponseMapper.mapFrom(trainee))
                .thenReturn(expectedResponse);

        UpdateTraineeProfileResponse response =
                traineeService.update(request);

        assertThat(response).isSameAs(expectedResponse);
        assertThat(user.getFirstName()).isEqualTo("Johnny");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getIsActive()).isTrue();
        assertThat(trainee.getDateOfBirth()).isEqualTo(dateOfBirth);
        assertThat(trainee.getAddress()).isEqualTo("Almaty");

        verify(updateResponseMapper).mapFrom(trainee);
    }

    @Test
    void updateShouldThrowExceptionWhenTraineeNotFound() {
        UpdateTraineeProfileRequest request =
                mock(UpdateTraineeProfileRequest.class);

        when(request.userName()).thenReturn("unknown");
        when(traineeRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.update(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainee unknown not found");
    }

    @Test
    void deleteShouldDeleteTrainee() {
        UsernameRequest request = mock(UsernameRequest.class);

        when(request.userName()).thenReturn("john");
        Trainee trainee = new Trainee();
        trainee.setTrainings(new ArrayList<>());
        when(traineeRepository.findByUsername("john")).thenReturn(Optional.of(trainee));
        when(traineeRepository.delete("john")).thenReturn(true);

        traineeService.delete(request);

        verify(traineeRepository).delete("john");
    }

    @Test
    void deleteShouldThrowExceptionWhenTraineeNotFound() {
        UsernameRequest request = mock(UsernameRequest.class);

        when(request.userName()).thenReturn("unknown");
        when(traineeRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.delete(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainee unknown not found");
    }

    @Test
    void findByUsernameShouldReturnMappedProfile() {
        UsernameRequest request = mock(UsernameRequest.class);
        Trainee trainee = new Trainee();

        GetTraineeProfileResponse expectedResponse =
                mock(GetTraineeProfileResponse.class);

        when(request.userName()).thenReturn("john");
        when(traineeRepository.findByUsername("john"))
                .thenReturn(Optional.of(trainee));
        when(getProfileResponseMapper.mapFrom(trainee))
                .thenReturn(expectedResponse);

        GetTraineeProfileResponse response =
                traineeService.findByUsername(request);

        assertThat(response).isSameAs(expectedResponse);

        verify(getProfileResponseMapper).mapFrom(trainee);
    }

    @Test
    void findByUsernameShouldThrowExceptionWhenNotFound() {
        UsernameRequest request = mock(UsernameRequest.class);

        when(request.userName()).thenReturn("unknown");
        when(traineeRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.findByUsername(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainee unknown not found");
    }

    @Test
    void switchActiveShouldChangeUserActiveStatus() {
        SwitchActiveFlagRequest request =
                mock(SwitchActiveFlagRequest.class);

        User user = new User();
        user.setIsActive(false);

        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(request.userName()).thenReturn("john");
        when(request.isActive()).thenReturn(true);
        when(traineeRepository.findByUsername("john"))
                .thenReturn(Optional.of(trainee));

        traineeService.switchActive(request);

        assertThat(user.getIsActive()).isTrue();
    }

    @Test
    void findByIdShouldReturnTrainee() {
        Trainee trainee = new Trainee();

        when(traineeRepository.findById(1L))
                .thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.findById(1L);

        assertThat(result).contains(trainee);
    }

    @Test
    void findAllShouldReturnAllTrainees() {
        List<Trainee> trainees =
                List.of(new Trainee(), new Trainee());

        when(traineeRepository.findAll()).thenReturn(trainees);

        List<Trainee> result = traineeService.findAll();

        assertThat(result).isEqualTo(trainees);
    }

    @Test
    void updateTrainersListShouldReplaceTrainers() {
        UpdateTraineeTrainerListRequest request =
                mock(UpdateTraineeTrainerListRequest.class);

        UsernameRequest trainerRequest =
                mock(UsernameRequest.class);

        User traineeUser = new User();
        traineeUser.setUserName("trainee");

        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);
        trainee.setTrainers(new ArrayList<>());

        Trainer oldTrainer = new Trainer();
        Trainer newTrainer = new Trainer();

        trainee.addTrainer(oldTrainer);

        TrainerSummaryResponse summary =
                mock(TrainerSummaryResponse.class);

        when(request.traineeUserName()).thenReturn("trainee");
        when(request.trainersList())
                .thenReturn(List.of(trainerRequest));
        when(trainerRequest.userName()).thenReturn("trainer1");

        when(traineeRepository.findByUsername("trainee"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("trainer1"))
                .thenReturn(Optional.of(newTrainer));

        when(trainerSummaryResponseMapper.mapFrom(newTrainer))
                .thenReturn(summary);

        List<TrainerSummaryResponse> result =
                traineeService.updateTrainersList(request);

        assertThat(trainee.getTrainers())
                .containsExactly(newTrainer);

        assertThat(result)
                .containsExactly(summary);
    }
}