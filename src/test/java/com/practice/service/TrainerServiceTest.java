package com.practice.service;

import com.practice.dao.TraineeRepositoryImpl;
import com.practice.dao.TrainerRepositoryImpl;
import com.practice.dao.TrainingTypeRepository;
import com.practice.dto.request.*;
import com.practice.dto.response.GetTrainerProfileResponse;
import com.practice.dto.response.RegistrationResponse;
import com.practice.dto.response.TrainerSummaryResponse;
import com.practice.dto.response.UpdateTrainerProfileResponse;
import com.practice.entity.Trainer;
import com.practice.entity.TrainingType;
import com.practice.entity.User;
import com.practice.mapper.request.TrainerRegistrationRequestMapper;
import com.practice.mapper.response.GetTrainerProfileResponseMapper;
import com.practice.mapper.response.TrainerRegistrationResponseMapper;
import com.practice.mapper.response.TrainerSummaryResponseMapper;
import com.practice.mapper.response.UpdateTrainerProfileResponseMapper;
import com.practice.utility.Credentials;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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
    private TrainerRegistrationRequestMapper trainerRegistrationRequestMapper;

    @Mock
    private TrainerRegistrationResponseMapper registrationResponseMapper;

    @Mock
    private UpdateTrainerProfileResponseMapper updateTrainerProfileResponseMapper;

    @Mock
    private GetTrainerProfileResponseMapper getTrainerProfileResponseMapper;

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
        trainerService.setTrainerRegistrationRequestMapper(
                trainerRegistrationRequestMapper
        );
        trainerService.setRegistrationResponseMapper(
                registrationResponseMapper
        );
        trainerService.setUpdateTrainerProfileResponseMapper(
                updateTrainerProfileResponseMapper
        );
        trainerService.setGetTrainerProfileResponseMapper(
                getTrainerProfileResponseMapper
        );
        trainerService.setTrainerSummaryResponseMapper(
                trainerSummaryResponseMapper
        );
    }

    @Test
    void create_shouldCreateTrainerAndReturnRegistrationResponse() {
        TrainerRegistrationRequest request =
                mock(TrainerRegistrationRequest.class);

        Trainer trainerToCreate = mock(Trainer.class);
        Trainer savedTrainer = mock(Trainer.class);
        User user = mock(User.class);
        TrainingType trainingType = mock(TrainingType.class);
        RegistrationResponse expectedResponse =
                mock(RegistrationResponse.class);

        Credentials credentials =
                new Credentials("John.Doe", "generatedPassword");

        when(request.specializationId()).thenReturn(1L);

        when(trainerRegistrationRequestMapper.mapFrom(request))
                .thenReturn(trainerToCreate);

        when(trainingTypeRepository.findById(1L))
                .thenReturn(Optional.of(trainingType));

        when(trainerToCreate.getUser())
                .thenReturn(user);

        when(user.getFirstName())
                .thenReturn("John");

        when(user.getLastName())
                .thenReturn("Doe");

        when(userService.generateCredentials("John", "Doe"))
                .thenReturn(credentials);

        when(trainerRepository.save(trainerToCreate))
                .thenReturn(savedTrainer);

        when(registrationResponseMapper.mapFrom(savedTrainer))
                .thenReturn(expectedResponse);

        RegistrationResponse actualResponse =
                trainerService.create(request);

        assertThat(actualResponse).isSameAs(expectedResponse);

        verify(trainerRegistrationRequestMapper)
                .mapFrom(request);

        verify(trainingTypeRepository)
                .findById(1L);

        verify(trainerToCreate)
                .setTrainingType(trainingType);

        verify(userService)
                .generateCredentials("John", "Doe");

        verify(user)
                .setUserName("John.Doe");

        verify(user)
                .setPassword("generatedPassword");

        verify(user)
                .setIsActive(true);

        verify(trainerRepository)
                .save(trainerToCreate);

        verify(registrationResponseMapper)
                .mapFrom(savedTrainer);
    }

    @Test
    void create_shouldPerformOperationsInCorrectOrder() {
        TrainerRegistrationRequest request =
                mock(TrainerRegistrationRequest.class);

        Trainer trainer = mock(Trainer.class);
        User user = mock(User.class);
        TrainingType trainingType = mock(TrainingType.class);
        RegistrationResponse response =
                mock(RegistrationResponse.class);

        Credentials credentials =
                new Credentials("John.Doe", "password");

        when(request.specializationId()).thenReturn(1L);

        when(trainerRegistrationRequestMapper.mapFrom(request))
                .thenReturn(trainer);

        when(trainingTypeRepository.findById(1L))
                .thenReturn(Optional.of(trainingType));

        when(trainer.getUser()).thenReturn(user);
        when(user.getFirstName()).thenReturn("John");
        when(user.getLastName()).thenReturn("Doe");

        when(userService.generateCredentials("John", "Doe"))
                .thenReturn(credentials);

        when(trainerRepository.save(trainer))
                .thenReturn(trainer);

        when(registrationResponseMapper.mapFrom(trainer))
                .thenReturn(response);

        trainerService.create(request);

        InOrder inOrder = inOrder(
                trainerRegistrationRequestMapper,
                trainingTypeRepository,
                trainer,
                userService,
                user,
                trainerRepository,
                registrationResponseMapper
        );

        inOrder.verify(trainerRegistrationRequestMapper)
                .mapFrom(request);

        inOrder.verify(trainingTypeRepository)
                .findById(1L);

        inOrder.verify(trainer)
                .setTrainingType(trainingType);

        inOrder.verify(userService)
                .generateCredentials("John", "Doe");

        inOrder.verify(user)
                .setUserName("John.Doe");

        inOrder.verify(user)
                .setPassword("password");

        inOrder.verify(user)
                .setIsActive(true);

        inOrder.verify(trainerRepository)
                .save(trainer);

        inOrder.verify(registrationResponseMapper)
                .mapFrom(trainer);
    }

    @Test
    void create_shouldThrowExceptionWhenTrainingTypeNotFound() {
        TrainerRegistrationRequest request =
                mock(TrainerRegistrationRequest.class);

        Trainer trainer = mock(Trainer.class);

        when(request.specializationId()).thenReturn(99L);

        when(trainerRegistrationRequestMapper.mapFrom(request))
                .thenReturn(trainer);

        when(trainingTypeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "TrainingType with id 99 does not exist"
                );

        verify(trainerRegistrationRequestMapper)
                .mapFrom(request);

        verify(trainingTypeRepository)
                .findById(99L);

        verify(trainer, never())
                .setTrainingType(any());

        verifyNoInteractions(userService);
        verifyNoInteractions(registrationResponseMapper);

        verify(trainerRepository, never())
                .save(any());
    }

    @Test
    void update_shouldUpdateTrainerAndReturnResponse() {
        UpdateTrainerProfileRequest request =
                mock(UpdateTrainerProfileRequest.class);

        Trainer trainer = mock(Trainer.class);
        User user = mock(User.class);

        UpdateTrainerProfileResponse expectedResponse =
                mock(UpdateTrainerProfileResponse.class);

        when(request.userName())
                .thenReturn("John.Doe");

        when(request.firstName())
                .thenReturn("Jonathan");

        when(request.lastName())
                .thenReturn("Smith");

        when(request.isActive())
                .thenReturn(false);

        when(trainerRepository.findByUsername("John.Doe"))
                .thenReturn(Optional.of(trainer));

        when(trainer.getUser())
                .thenReturn(user);

        when(updateTrainerProfileResponseMapper.mapFrom(trainer))
                .thenReturn(expectedResponse);

        UpdateTrainerProfileResponse actualResponse =
                trainerService.update(request);

        assertThat(actualResponse)
                .isSameAs(expectedResponse);

        verify(trainerRepository)
                .findByUsername("John.Doe");

        verify(user)
                .setUserName("John.Doe");

        verify(user)
                .setFirstName("Jonathan");

        verify(user)
                .setLastName("Smith");

        verify(user)
                .setIsActive(false);

        verify(updateTrainerProfileResponseMapper)
                .mapFrom(trainer);
    }

    @Test
    void update_shouldThrowExceptionWhenTrainerNotFound() {
        UpdateTrainerProfileRequest request =
                mock(UpdateTrainerProfileRequest.class);

        when(request.userName())
                .thenReturn("unknown");

        when(trainerRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.update(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainer unknown not found");

        verify(trainerRepository)
                .findByUsername("unknown");

        verifyNoInteractions(updateTrainerProfileResponseMapper);
    }

    @Test
    void findById_shouldReturnTrainer() {
        Trainer trainer = mock(Trainer.class);

        when(trainerRepository.findById(1L))
                .thenReturn(Optional.of(trainer));

        Optional<Trainer> result =
                trainerService.findById(1L);

        assertThat(result)
                .isPresent()
                .contains(trainer);

        verify(trainerRepository)
                .findById(1L);
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenTrainerNotFound() {
        when(trainerRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Trainer> result =
                trainerService.findById(99L);

        assertThat(result)
                .isEmpty();

        verify(trainerRepository)
                .findById(99L);
    }

    @Test
    void findAll_shouldReturnAllTrainers() {
        Trainer firstTrainer = mock(Trainer.class);
        Trainer secondTrainer = mock(Trainer.class);

        List<Trainer> expected =
                List.of(firstTrainer, secondTrainer);

        when(trainerRepository.findAll())
                .thenReturn(expected);

        List<Trainer> result =
                trainerService.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactly(
                        firstTrainer,
                        secondTrainer
                );

        verify(trainerRepository)
                .findAll();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoTrainersExist() {
        when(trainerRepository.findAll())
                .thenReturn(List.of());

        List<Trainer> result =
                trainerService.findAll();

        assertThat(result)
                .isEmpty();

        verify(trainerRepository)
                .findAll();
    }

    @Test
    void findByUsername_shouldReturnMappedTrainerProfile() {
        UsernameRequest request =
                mock(UsernameRequest.class);

        Trainer trainer = mock(Trainer.class);

        GetTrainerProfileResponse expectedResponse =
                mock(GetTrainerProfileResponse.class);

        when(request.userName())
                .thenReturn("John.Doe");

        when(trainerRepository.findByUsername("John.Doe"))
                .thenReturn(Optional.of(trainer));

        when(getTrainerProfileResponseMapper.mapFrom(trainer))
                .thenReturn(expectedResponse);

        GetTrainerProfileResponse actualResponse =
                trainerService.findByUsername(request);

        assertThat(actualResponse)
                .isSameAs(expectedResponse);

        verify(trainerRepository)
                .findByUsername("John.Doe");

        verify(getTrainerProfileResponseMapper)
                .mapFrom(trainer);
    }

    @Test
    void findByUsername_shouldThrowExceptionWhenTrainerNotFound() {
        UsernameRequest request =
                mock(UsernameRequest.class);

        when(request.userName())
                .thenReturn("unknown");

        when(trainerRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> trainerService.findByUsername(request)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainer unknown not found");

        verify(trainerRepository)
                .findByUsername("unknown");

        verifyNoInteractions(getTrainerProfileResponseMapper);
    }

    @Test
    void findAllNotAssignedByTraineeUserName_shouldReturnMappedTrainers() {
        UsernameRequest request =
                mock(UsernameRequest.class);

        Trainer firstTrainer = mock(Trainer.class);
        Trainer secondTrainer = mock(Trainer.class);

        TrainerSummaryResponse firstResponse =
                mock(TrainerSummaryResponse.class);

        TrainerSummaryResponse secondResponse =
                mock(TrainerSummaryResponse.class);

        when(request.userName())
                .thenReturn("trainee.user");

        when(
                trainerRepository.findAllNotAssignedByTraineeUserName(
                        "trainee.user"
                )
        ).thenReturn(
                List.of(firstTrainer, secondTrainer)
        );

        when(trainerSummaryResponseMapper.mapFrom(firstTrainer))
                .thenReturn(firstResponse);

        when(trainerSummaryResponseMapper.mapFrom(secondTrainer))
                .thenReturn(secondResponse);

        List<TrainerSummaryResponse> result =
                trainerService.findAllNotAssignedByTraineeUserName(
                        request
                );

        assertThat(result)
                .hasSize(2)
                .containsExactly(
                        firstResponse,
                        secondResponse
                );

        verify(trainerRepository)
                .findAllNotAssignedByTraineeUserName(
                        "trainee.user"
                );

        verify(trainerSummaryResponseMapper)
                .mapFrom(firstTrainer);

        verify(trainerSummaryResponseMapper)
                .mapFrom(secondTrainer);
    }

    @Test
    void findAllNotAssignedByTraineeUserName_shouldReturnEmptyList() {
        UsernameRequest request =
                mock(UsernameRequest.class);

        when(request.userName())
                .thenReturn("trainee.user");

        when(
                trainerRepository.findAllNotAssignedByTraineeUserName(
                        "trainee.user"
                )
        ).thenReturn(List.of());

        List<TrainerSummaryResponse> result =
                trainerService.findAllNotAssignedByTraineeUserName(
                        request
                );

        assertThat(result)
                .isEmpty();

        verifyNoInteractions(trainerSummaryResponseMapper);
    }

    @Test
    void switchActive_shouldChangeActiveFlag() {
        SwitchActiveFlagRequest request =
                mock(SwitchActiveFlagRequest.class);

        Trainer trainer = mock(Trainer.class);
        User user = mock(User.class);

        when(request.userName())
                .thenReturn("John.Doe");

        when(request.isActive())
                .thenReturn(false);

        when(trainerRepository.findByUsername("John.Doe"))
                .thenReturn(Optional.of(trainer));

        when(trainer.getUser())
                .thenReturn(user);

        trainerService.switchActive(request);

        verify(trainerRepository)
                .findByUsername("John.Doe");

        verify(user)
                .setIsActive(false);
    }

    @Test
    void switchActive_shouldActivateTrainer() {
        SwitchActiveFlagRequest request =
                mock(SwitchActiveFlagRequest.class);

        Trainer trainer = mock(Trainer.class);
        User user = mock(User.class);

        when(request.userName())
                .thenReturn("John.Doe");

        when(request.isActive())
                .thenReturn(true);

        when(trainerRepository.findByUsername("John.Doe"))
                .thenReturn(Optional.of(trainer));

        when(trainer.getUser())
                .thenReturn(user);

        trainerService.switchActive(request);

        verify(user)
                .setIsActive(true);
    }

    @Test
    void switchActive_shouldThrowExceptionWhenTrainerNotFound() {
        SwitchActiveFlagRequest request =
                mock(SwitchActiveFlagRequest.class);

        when(request.userName())
                .thenReturn("unknown");

        when(trainerRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> trainerService.switchActive(request)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainer unknown not found");

        verify(trainerRepository)
                .findByUsername("unknown");
    }
}