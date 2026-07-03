package com.practice.service;

import com.practice.dao.TraineeRepositoryImpl;
import com.practice.dao.TrainerRepositoryImpl;
import com.practice.dao.TrainingTypeRepository;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.TrainingType;
import com.practice.entity.User;
import com.practice.exception.AuthenticationException;
import com.practice.utility.ServiceUtility;
import com.practice.utility.Validator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepositoryImpl trainerRepository;

    @Mock
    private TraineeRepositoryImpl traineeRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    private TrainerService service;

    private User user;
    private Trainer trainer;
    private TrainingType cardio;

    @BeforeEach
    void setUp() {
        service = new TrainerService();
        service.setTrainerRepository(trainerRepository);
        service.setTraineeRepository(traineeRepository);
        service.setTrainingTypeRepository(trainingTypeRepository);

        cardio = TrainingType.builder().id(1L).name("Cardio").build();

        user = User.builder()
                .id(1L).firstName("John").lastName("Roe")
                .userName("john.roe").password("secret").isActive(true)
                .build();

        trainer = Trainer.builder().id(1L).user(user).trainingType(cardio).build();
    }

    @Test
    void create_shouldGenerateCredentialsAndReturnSavedId_whenTrainingTypeExists() {
        Trainer newTrainer = Trainer.builder()
                .user(User.builder().firstName("John").lastName("Roe").build())
                .trainingType(cardio)
                .build();

        when(trainingTypeRepository.findAll()).thenReturn(List.of(cardio));
        when(traineeRepository.findAll()).thenReturn(List.of());
        when(trainerRepository.findAll()).thenReturn(List.of(trainer));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class);
             MockedStatic<ServiceUtility> utilityMock = mockStatic(ServiceUtility.class)) {

            utilityMock.when(() -> ServiceUtility.generateUserName(eq("John"), eq("Roe"), anyList()))
                    .thenReturn("john.roe1");
            utilityMock.when(ServiceUtility::generatePassword).thenReturn("generatedPass");

            long id = service.create(newTrainer);

            assertThat(id).isEqualTo(2L);
            assertThat(newTrainer.getUser().getUserName()).isEqualTo("john.roe1");
            assertThat(newTrainer.getUser().getPassword()).isEqualTo("generatedPass");
            validatorMock.verify(() -> Validator.validateTrainerForCreate(newTrainer));
            verify(trainerRepository).save(newTrainer);
        }
    }

    @Test
    void create_shouldThrowIllegalArgumentException_whenTrainingTypeDoesNotExist() {
        Trainer newTrainer = Trainer.builder()
                .user(User.builder().firstName("John").lastName("Roe").build())
                .trainingType(TrainingType.builder().id(99L).name("Unknown").build())
                .build();

        when(trainingTypeRepository.findAll()).thenReturn(List.of(cardio));

        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            assertThatThrownBy(() -> service.create(newTrainer))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(trainerRepository, never()).save(any());
        }
    }

    @Test
    void update_shouldReturnTrue_whenAuthenticatedAndTrainerExists() {
        when(trainerRepository.findByUsername("john.roe")).thenReturn(Optional.of(trainer));
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));

        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            boolean result = service.update("john.roe", "secret", trainer);

            assertThat(result).isTrue();
            verify(trainerRepository).update(trainer);
        }
    }

    @Test
    void update_shouldThrowAuthenticationException_whenPasswordDoesNotMatch() {
        when(trainerRepository.findByUsername("john.roe")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> service.update("john.roe", "wrongPass", trainer))
                .isInstanceOf(AuthenticationException.class);

        verify(trainerRepository, never()).update(any());
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenTrainerNotFound() {
        when(trainerRepository.findByUsername("john.roe")).thenReturn(Optional.of(trainer));
        when(trainerRepository.findById(1L)).thenReturn(Optional.empty());

        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            assertThatThrownBy(() -> service.update("john.roe", "secret", trainer))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(trainerRepository, never()).update(any());
        }
    }

    @Test
    void findById_shouldReturnTrainer_whenFound() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = service.findById(1L);

        assertThat(result).isPresent().contains(trainer);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Trainer> result = service.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllTrainers() {
        when(trainerRepository.findAll()).thenReturn(List.of(trainer));

        List<Trainer> result = service.findAll();

        assertThat(result).containsExactly(trainer);
    }

    @Test
    void findByUsername_shouldReturnTrainer_whenAuthenticated() {
        when(trainerRepository.findByUsername("john.roe")).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = service.findByUsername("john.roe", "secret");

        assertThat(result).isPresent().contains(trainer);
    }

    @Test
    void findByUsername_shouldThrowAuthenticationException_whenNotAuthenticated() {
        when(trainerRepository.findByUsername("john.roe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByUsername("john.roe", "secret"))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void findAllNotAssignedByTraineeUserName_shouldReturnUnassignedTrainers_whenTraineeAuthenticated() {
        User traineeUser = User.builder()
                .firstName("Jane").lastName("Doe").userName("jane.doe").password("traineePass").build();
        Trainee traineeEntity = Trainee.builder().id(5L).user(traineeUser).build();

        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.of(traineeEntity));
        when(trainerRepository.findAllNotAssignedByTraineeUserName("jane.doe")).thenReturn(List.of(trainer));

        List<Trainer> result = service.findAllNotAssignedByTraineeUserName("jane.doe", "traineePass");

        assertThat(result).containsExactly(trainer);
    }

    @Test
    void findAllNotAssignedByTraineeUserName_shouldThrowAuthenticationException_whenTraineeNotAuthenticated() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findAllNotAssignedByTraineeUserName("jane.doe", "traineePass"))
                .isInstanceOf(AuthenticationException.class);

        verify(trainerRepository, never()).findAllNotAssignedByTraineeUserName(anyString());
    }

    @Test
    void switchActive_shouldToggleActive_whenAuthenticated() {
        when(trainerRepository.findByUsername("john.roe")).thenReturn(Optional.of(trainer));

        service.switchActive("john.roe", "secret");

        verify(trainerRepository).switchActive(1L);
    }

    @Test
    void switchActive_shouldThrowAuthenticationException_whenNotAuthenticated() {
        when(trainerRepository.findByUsername("john.roe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.switchActive("john.roe", "secret"))
                .isInstanceOf(AuthenticationException.class);

        verify(trainerRepository, never()).switchActive(any());
    }

    @Test
    void changePassword_shouldUpdatePassword_whenAuthenticated() {
        when(trainerRepository.findByUsername("john.roe")).thenReturn(Optional.of(trainer));

        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            service.changePassword("john.roe", "secret", "newSecret");

            assertThat(trainer.getUser().getPassword()).isEqualTo("newSecret");
            verify(trainerRepository).update(trainer);
        }
    }

    @Test
    void changePassword_shouldThrowAuthenticationException_whenOldPasswordWrong() {
        when(trainerRepository.findByUsername("john.roe")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> service.changePassword("john.roe", "wrongPass", "newSecret"))
                .isInstanceOf(AuthenticationException.class);

        verify(trainerRepository, never()).update(any());
    }

    @Test
    void isMatching_shouldReturnTrue_whenPasswordMatches() {
        when(trainerRepository.findByUsername("john.roe")).thenReturn(Optional.of(trainer));

        boolean result = service.isMatching("john.roe", "secret");

        assertThat(result).isTrue();
    }

    @Test
    void isMatching_shouldReturnFalse_whenPasswordDoesNotMatch() {
        when(trainerRepository.findByUsername("john.roe")).thenReturn(Optional.of(trainer));

        boolean result = service.isMatching("john.roe", "wrongPass");

        assertThat(result).isFalse();
    }

    @Test
    void isMatching_shouldReturnFalse_whenUserNotFound() {
        when(trainerRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        boolean result = service.isMatching("unknown", "secret");

        assertThat(result).isFalse();
    }
}