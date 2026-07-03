package com.practice.service;

import com.practice.dao.TraineeRepository;
import com.practice.dao.TrainerRepository;
import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
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

import java.util.ArrayList;
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
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    private TraineeService service;

    private User user;
    private Trainee trainee;

    @BeforeEach
    void setUp() {
        service = new TraineeService();
        service.setTraineeRepository(traineeRepository);
        service.setTrainerRepository(trainerRepository);

        user = User.builder()
                .id(1L).firstName("Jane").lastName("Doe")
                .userName("jane.doe").password("secret").isActive(true)
                .build();

        trainee = Trainee.builder().id(1L).user(user).trainers(new ArrayList<>()).build();
    }

    @Test
    void create_shouldGenerateCredentialsAndReturnSavedId() {
        Trainee newTrainee = Trainee.builder()
                .user(User.builder().firstName("Jane").lastName("Doe").build())
                .build();

        when(traineeRepository.findAll()).thenReturn(List.of(trainee));
        when(trainerRepository.findAll()).thenReturn(List.of());
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class);
             MockedStatic<ServiceUtility> utilityMock = mockStatic(ServiceUtility.class)) {

            utilityMock.when(() -> ServiceUtility.generateUserName(eq("Jane"), eq("Doe"), anyList()))
                    .thenReturn("jane.doe1");
            utilityMock.when(ServiceUtility::generatePassword).thenReturn("generatedPass");

            long id = service.create(newTrainee);

            assertThat(id).isEqualTo(2L);
            assertThat(newTrainee.getUser().getUserName()).isEqualTo("jane.doe1");
            assertThat(newTrainee.getUser().getPassword()).isEqualTo("generatedPass");
            assertThat(newTrainee.getUser().getIsActive()).isTrue();
            validatorMock.verify(() -> Validator.validateTraineeForCreate(newTrainee));
            verify(traineeRepository).save(newTrainee);
        }
    }

    @Test
    void update_shouldReturnTrue_whenAuthenticatedAndTraineeExists() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            boolean result = service.update("jane.doe", "secret", trainee);

            assertThat(result).isTrue();
            verify(traineeRepository).update(trainee);
        }
    }

    @Test
    void update_shouldThrowAuthenticationException_whenPasswordDoesNotMatch() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> service.update("jane.doe", "wrongPass", trainee))
                .isInstanceOf(AuthenticationException.class);

        verify(traineeRepository, never()).update(any());
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenTraineeNotFound() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.findById(1L)).thenReturn(Optional.empty());

        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            assertThatThrownBy(() -> service.update("jane.doe", "secret", trainee))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(traineeRepository, never()).update(any());
        }
    }

    @Test
    void delete_shouldReturnTrue_whenAuthenticatedAndDeleted() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.delete("jane.doe")).thenReturn(true);

        boolean result = service.delete("jane.doe", "secret");

        assertThat(result).isTrue();
    }

    @Test
    void delete_shouldReturnFalse_whenAuthenticatedButNothingDeleted() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.delete("jane.doe")).thenReturn(false);

        boolean result = service.delete("jane.doe", "secret");

        assertThat(result).isFalse();
    }

    @Test
    void delete_shouldThrowAuthenticationException_whenNotAuthenticated() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("jane.doe", "secret"))
                .isInstanceOf(AuthenticationException.class);

        verify(traineeRepository, never()).delete(anyString());
    }

    @Test
    void findById_shouldReturnTrainee_whenFound() {
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = service.findById(1L);

        assertThat(result).isPresent().contains(trainee);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        when(traineeRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Trainee> result = service.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByUsername_shouldReturnTrainee_whenAuthenticated() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = service.findByUsername("jane.doe", "secret");

        assertThat(result).isPresent().contains(trainee);
    }

    @Test
    void findByUsername_shouldThrowAuthenticationException_whenNotAuthenticated() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByUsername("jane.doe", "secret"))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void findAll_shouldReturnAllTrainees() {
        when(traineeRepository.findAll()).thenReturn(List.of(trainee));

        List<Trainee> result = service.findAll();

        assertThat(result).containsExactly(trainee);
    }

    @Test
    void switchActive_shouldToggleActive_whenAuthenticated() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.of(trainee));

        service.switchActive("jane.doe", "secret");

        verify(traineeRepository).switchActive(1L);
    }

    @Test
    void switchActive_shouldThrowAuthenticationException_whenNotAuthenticated() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.switchActive("jane.doe", "secret"))
                .isInstanceOf(AuthenticationException.class);

        verify(traineeRepository, never()).switchActive(any());
    }

    @Test
    void changePassword_shouldUpdatePassword_whenAuthenticated() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.of(trainee));

        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            service.changePassword("jane.doe", "secret", "newSecret");

            assertThat(trainee.getUser().getPassword()).isEqualTo("newSecret");
            verify(traineeRepository).update(trainee);
        }
    }

    @Test
    void changePassword_shouldThrowAuthenticationException_whenOldPasswordWrong() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.of(trainee));

        assertThatThrownBy(() -> service.changePassword("jane.doe", "wrongPass", "newSecret"))
                .isInstanceOf(AuthenticationException.class);

        verify(traineeRepository, never()).update(any());
    }

    @Test
    void updateTrainersList_shouldReplaceTrainers_whenAuthenticatedAndAllTrainersExist() {
        Trainer trainer1 = Trainer.builder().id(10L).trainees(new ArrayList<>()).build();
        Trainer trainer2 = Trainer.builder().id(20L).trainees(new ArrayList<>()).build();

        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(10L)).thenReturn(Optional.of(trainer1));
        when(trainerRepository.findById(20L)).thenReturn(Optional.of(trainer2));

        service.updateTrainersList("jane.doe", "secret", List.of(10L, 20L));

        assertThat(trainee.getTrainers()).containsExactly(trainer1, trainer2);
        verify(traineeRepository).update(trainee);
    }

    @Test
    void updateTrainersList_shouldThrowEntityNotFoundException_whenTrainerIdMissing() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTrainersList("jane.doe", "secret", List.of(99L)))
                .isInstanceOf(EntityNotFoundException.class);

        verify(traineeRepository, never()).update(any());
    }

    @Test
    void updateTrainersList_shouldThrowAuthenticationException_whenNotAuthenticated() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTrainersList("jane.doe", "secret", List.of(10L)))
                .isInstanceOf(AuthenticationException.class);

        verify(trainerRepository, never()).findById(any());
    }

    @Test
    void isMatching_shouldReturnTrue_whenPasswordMatches() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.of(trainee));

        boolean result = service.isMatching("jane.doe", "secret");

        assertThat(result).isTrue();
    }

    @Test
    void isMatching_shouldReturnFalse_whenPasswordDoesNotMatch() {
        when(traineeRepository.findByUsername("jane.doe")).thenReturn(Optional.of(trainee));

        boolean result = service.isMatching("jane.doe", "wrongPass");

        assertThat(result).isFalse();
    }

    @Test
    void isMatching_shouldReturnFalse_whenUserNotFound() {
        when(traineeRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        boolean result = service.isMatching("unknown", "secret");

        assertThat(result).isFalse();
    }
}