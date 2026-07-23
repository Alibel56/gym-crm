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
import com.practice.mapper.request.TraineeRegistrationRequestMapper;
import com.practice.mapper.response.GetTraineeProfileResponseMapper;
import com.practice.mapper.response.TraineeRegistrationResponseMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private TraineeRegistrationRequestMapper traineeRegistrationRequestMapper;

    @Mock
    private TraineeRegistrationResponseMapper registrationResponseMapper;

    @Mock
    private UpdateTraineeProfileResponseMapper updateTraineeProfileResponseMapper;

    @Mock
    private GetTraineeProfileResponseMapper getTraineeProfileResponseMapper;

    @Mock
    private TrainerSummaryResponseMapper trainerSummaryResponseMapper;

    @Mock
    private GymMetrics gymMetrics;

    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        traineeService = new TraineeService();

        traineeService.setUserService(userService);
        traineeService.setTraineeRepository(traineeRepository);
        traineeService.setTrainerRepository(trainerRepository);
        traineeService.setGymMetrics(gymMetrics);
        traineeService.setTraineeRegistrationRequestMapper(
                traineeRegistrationRequestMapper
        );
        traineeService.setRegistrationResponseMapper(
                registrationResponseMapper
        );
        traineeService.setUpdateTraineeProfileResponseMapper(
                updateTraineeProfileResponseMapper
        );
        traineeService.setGetTraineeProfileResponseMapper(
                getTraineeProfileResponseMapper
        );
        traineeService.setTrainerSummaryResponseMapper(
                trainerSummaryResponseMapper
        );
    }

    @Test
    void create_shouldGenerateCredentialsSaveTraineeAndReturnResponse() {
        TraineeRegistrationRequest request =
                new TraineeRegistrationRequest(
                        "Alibek",
                        "Aglanov",
                        LocalDate.of(2006, 3, 2),
                        "Almaty"
                );

        User user = User.builder()
                .firstName("Alibek")
                .lastName("Aglanov")
                .build();

        Trainee trainee = Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(2006, 3, 2))
                .address("Almaty")
                .build();

        Credentials credentials =
                new Credentials("Alibek.Aglanov", "generatedPassword");

        RegistrationResponse expectedResponse =
                mock(RegistrationResponse.class);

        when(traineeRegistrationRequestMapper.mapFrom(request))
                .thenReturn(trainee);

        when(userService.generateCredentials("Alibek", "Aglanov"))
                .thenReturn(credentials);

        when(traineeRepository.save(trainee))
                .thenReturn(trainee);

        when(registrationResponseMapper.mapFrom(trainee))
                .thenReturn(expectedResponse);

        RegistrationResponse actualResponse =
                traineeService.create(request);

        assertThat(actualResponse).isSameAs(expectedResponse);

        verify(gymMetrics).recordRegistration("trainee");

        assertThat(user.getUserName())
                .isEqualTo("Alibek.Aglanov");

        assertThat(user.getPassword())
                .isEqualTo("generatedPassword");

        assertThat(user.getIsActive())
                .isTrue();

        verify(traineeRegistrationRequestMapper)
                .mapFrom(request);

        verify(userService)
                .generateCredentials("Alibek", "Aglanov");

        verify(traineeRepository)
                .save(trainee);

        verify(registrationResponseMapper)
                .mapFrom(trainee);
    }

    @Test
    void update_shouldUpdateTraineeAndReturnResponse() {
        UpdateTraineeProfileRequest request =
                new UpdateTraineeProfileRequest(
                        "Alibek.Aglanov",
                        "NewName",
                        "NewLastName",
                        LocalDate.of(2005, 5, 10),
                        "Astana",
                        false
                );

        User user = User.builder()
                .userName("Alibek.Aglanov")
                .firstName("Alibek")
                .lastName("Aglanov")
                .isActive(true)
                .build();

        Trainee trainee = Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(2006, 3, 2))
                .address("Almaty")
                .build();

        UpdateTraineeProfileResponse expectedResponse =
                mock(UpdateTraineeProfileResponse.class);

        when(traineeRepository.findByUsername("Alibek.Aglanov"))
                .thenReturn(Optional.of(trainee));

        when(updateTraineeProfileResponseMapper.mapFrom(trainee))
                .thenReturn(expectedResponse);

        UpdateTraineeProfileResponse actualResponse =
                traineeService.update(request);

        assertThat(actualResponse).isSameAs(expectedResponse);

        assertThat(user.getFirstName())
                .isEqualTo("NewName");

        assertThat(user.getLastName())
                .isEqualTo("NewLastName");

        assertThat(user.getIsActive())
                .isFalse();

        assertThat(trainee.getDateOfBirth())
                .isEqualTo(LocalDate.of(2005, 5, 10));

        assertThat(trainee.getAddress())
                .isEqualTo("Astana");

        verify(traineeRepository)
                .findByUsername("Alibek.Aglanov");

        verify(updateTraineeProfileResponseMapper)
                .mapFrom(trainee);
    }

    @Test
    void update_shouldNotChangeOptionalFieldsWhenTheyAreNull() {
        UpdateTraineeProfileRequest request =
                new UpdateTraineeProfileRequest(
                        "Alibek.Aglanov",
                        "UpdatedName",
                        "UpdatedLastName",
                        null,
                        null,
                        true
                );

        LocalDate oldDate = LocalDate.of(2006, 3, 2);
        String oldAddress = "Almaty";

        User user = User.builder()
                .userName("Alibek.Aglanov")
                .firstName("Alibek")
                .lastName("Aglanov")
                .isActive(false)
                .build();

        Trainee trainee = Trainee.builder()
                .user(user)
                .dateOfBirth(oldDate)
                .address(oldAddress)
                .build();

        UpdateTraineeProfileResponse expectedResponse =
                mock(UpdateTraineeProfileResponse.class);

        when(traineeRepository.findByUsername("Alibek.Aglanov"))
                .thenReturn(Optional.of(trainee));

        when(updateTraineeProfileResponseMapper.mapFrom(trainee))
                .thenReturn(expectedResponse);

        UpdateTraineeProfileResponse actualResponse =
                traineeService.update(request);

        assertThat(actualResponse).isSameAs(expectedResponse);

        assertThat(trainee.getDateOfBirth())
                .isEqualTo(oldDate);

        assertThat(trainee.getAddress())
                .isEqualTo(oldAddress);

        assertThat(user.getFirstName())
                .isEqualTo("UpdatedName");

        assertThat(user.getLastName())
                .isEqualTo("UpdatedLastName");

        assertThat(user.getIsActive())
                .isTrue();
    }

    @Test
    void update_shouldThrowExceptionWhenTraineeNotFound() {
        UpdateTraineeProfileRequest request =
                new UpdateTraineeProfileRequest(
                        "unknown",
                        "Name",
                        "Surname",
                        null,
                        null,
                        true
                );

        when(traineeRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.update(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainee unknown not found");

        verifyNoInteractions(updateTraineeProfileResponseMapper);
    }

    @Test
    void delete_shouldDeleteTrainee() {
        UsernameRequest request =
                new UsernameRequest("Alibek.Aglanov");

        when(traineeRepository.delete("Alibek.Aglanov"))
                .thenReturn(true);

        traineeService.delete(request);

        verify(traineeRepository)
                .delete("Alibek.Aglanov");
    }

    @Test
    void delete_shouldThrowExceptionWhenTraineeNotFound() {
        UsernameRequest request =
                new UsernameRequest("unknown");

        when(traineeRepository.delete("unknown"))
                .thenReturn(false);

        assertThatThrownBy(() -> traineeService.delete(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainee unknown not found");

        verify(traineeRepository)
                .delete("unknown");
    }

    @Test
    void findById_shouldReturnTrainee() {
        Trainee trainee = Trainee.builder()
                .id(1L)
                .build();

        when(traineeRepository.findById(1L))
                .thenReturn(Optional.of(trainee));

        Optional<Trainee> result =
                traineeService.findById(1L);

        assertThat(result)
                .isPresent()
                .contains(trainee);

        verify(traineeRepository)
                .findById(1L);
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenTraineeNotFound() {
        when(traineeRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Trainee> result =
                traineeService.findById(99L);

        assertThat(result).isEmpty();

        verify(traineeRepository)
                .findById(99L);
    }

    @Test
    void findByUsername_shouldReturnMappedResponse() {
        UsernameRequest request =
                new UsernameRequest("Alibek.Aglanov");

        Trainee trainee = Trainee.builder()
                .id(1L)
                .build();

        GetTraineeProfileResponse expectedResponse =
                mock(GetTraineeProfileResponse.class);

        when(traineeRepository.findByUsername("Alibek.Aglanov"))
                .thenReturn(Optional.of(trainee));

        when(getTraineeProfileResponseMapper.mapFrom(trainee))
                .thenReturn(expectedResponse);

        GetTraineeProfileResponse actualResponse =
                traineeService.findByUsername(request);

        assertThat(actualResponse)
                .isSameAs(expectedResponse);

        verify(traineeRepository)
                .findByUsername("Alibek.Aglanov");

        verify(getTraineeProfileResponseMapper)
                .mapFrom(trainee);
    }

    @Test
    void findByUsername_shouldThrowExceptionWhenTraineeNotFound() {
        UsernameRequest request =
                new UsernameRequest("unknown");

        when(traineeRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> traineeService.findByUsername(request)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainee unknown not found");

        verifyNoInteractions(getTraineeProfileResponseMapper);
    }

    @Test
    void findAll_shouldReturnAllTrainees() {
        Trainee firstTrainee = Trainee.builder()
                .id(1L)
                .build();

        Trainee secondTrainee = Trainee.builder()
                .id(2L)
                .build();

        List<Trainee> expected =
                List.of(firstTrainee, secondTrainee);

        when(traineeRepository.findAll())
                .thenReturn(expected);

        List<Trainee> result =
                traineeService.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactly(firstTrainee, secondTrainee);

        verify(traineeRepository)
                .findAll();
    }

    @Test
    void switchActive_shouldChangeTraineeActiveFlag() {
        SwitchActiveFlagRequest request =
                new SwitchActiveFlagRequest(
                        "Alibek.Aglanov",
                        false
                );

        User user = User.builder()
                .userName("Alibek.Aglanov")
                .isActive(true)
                .build();

        Trainee trainee = Trainee.builder()
                .user(user)
                .build();

        when(traineeRepository.findByUsername("Alibek.Aglanov"))
                .thenReturn(Optional.of(trainee));

        traineeService.switchActive(request);

        assertThat(user.getIsActive())
                .isFalse();

        verify(traineeRepository)
                .findByUsername("Alibek.Aglanov");
    }

    @Test
    void switchActive_shouldThrowExceptionWhenTraineeNotFound() {
        SwitchActiveFlagRequest request =
                new SwitchActiveFlagRequest(
                        "unknown",
                        false
                );

        when(traineeRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> traineeService.switchActive(request)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainee unknown not found");
    }

    @Test
    void updateTrainersList_shouldReplaceExistingTrainersAndReturnResponses() {
        UpdateTraineeTrainerListRequest request =
                new UpdateTraineeTrainerListRequest(
                        "Alibek.Aglanov",
                        List.of(
                                new UsernameRequest("trainer.one"),
                                new UsernameRequest("trainer.two")
                        )
                );

        Trainer oldTrainer = createTrainer(
                1L,
                "old.trainer"
        );

        Trainer firstNewTrainer = createTrainer(
                2L,
                "trainer.one"
        );

        Trainer secondNewTrainer = createTrainer(
                3L,
                "trainer.two"
        );

        Trainee trainee = Trainee.builder()
                .id(1L)
                .user(
                        User.builder()
                                .userName("Alibek.Aglanov")
                                .build()
                )
                .trainers(new ArrayList<>())
                .build();

        /*
         * Добавляем через метод сущности, чтобы обе стороны связи
         * были синхронизированы.
         */
        trainee.addTrainer(oldTrainer);

        TrainerSummaryResponse firstResponse =
                mock(TrainerSummaryResponse.class);

        TrainerSummaryResponse secondResponse =
                mock(TrainerSummaryResponse.class);

        when(traineeRepository.findByUsername("Alibek.Aglanov"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("trainer.one"))
                .thenReturn(Optional.of(firstNewTrainer));

        when(trainerRepository.findByUsername("trainer.two"))
                .thenReturn(Optional.of(secondNewTrainer));

        when(trainerSummaryResponseMapper.mapFrom(firstNewTrainer))
                .thenReturn(firstResponse);

        when(trainerSummaryResponseMapper.mapFrom(secondNewTrainer))
                .thenReturn(secondResponse);

        List<TrainerSummaryResponse> result =
                traineeService.updateTrainersList(request);

        assertThat(trainee.getTrainers())
                .hasSize(2)
                .containsExactly(
                        firstNewTrainer,
                        secondNewTrainer
                )
                .doesNotContain(oldTrainer);

        assertThat(result)
                .containsExactly(
                        firstResponse,
                        secondResponse
                );

        verify(traineeRepository)
                .findByUsername("Alibek.Aglanov");

        verify(trainerRepository)
                .findByUsername("trainer.one");

        verify(trainerRepository)
                .findByUsername("trainer.two");

        verify(trainerSummaryResponseMapper)
                .mapFrom(firstNewTrainer);

        verify(trainerSummaryResponseMapper)
                .mapFrom(secondNewTrainer);
    }

    @Test
    void updateTrainersList_shouldThrowExceptionWhenTraineeNotFound() {
        UpdateTraineeTrainerListRequest request =
                new UpdateTraineeTrainerListRequest(
                        "unknown",
                        List.of(
                                new UsernameRequest("trainer.one")
                        )
                );

        when(traineeRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> traineeService.updateTrainersList(request)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainee unknown not found");

        verifyNoInteractions(trainerRepository);
        verifyNoInteractions(trainerSummaryResponseMapper);
    }

    @Test
    void updateTrainersList_shouldThrowExceptionWhenTrainerNotFound() {
        UpdateTraineeTrainerListRequest request =
                new UpdateTraineeTrainerListRequest(
                        "Alibek.Aglanov",
                        List.of(
                                new UsernameRequest("unknown.trainer")
                        )
                );

        Trainee trainee = Trainee.builder()
                .id(1L)
                .user(
                        User.builder()
                                .userName("Alibek.Aglanov")
                                .build()
                )
                .trainers(new ArrayList<>())
                .build();

        when(traineeRepository.findByUsername("Alibek.Aglanov"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("unknown.trainer"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> traineeService.updateTrainersList(request)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Trainer unknown.trainer not found");

        assertThat(trainee.getTrainers())
                .isEmpty();

        verifyNoInteractions(trainerSummaryResponseMapper);
    }

    @Test
    void updateTrainersList_shouldClearListWhenRequestListIsEmpty() {
        UpdateTraineeTrainerListRequest request =
                new UpdateTraineeTrainerListRequest(
                        "Alibek.Aglanov",
                        List.of()
                );

        Trainer oldTrainer = createTrainer(
                1L,
                "old.trainer"
        );

        Trainee trainee = Trainee.builder()
                .id(1L)
                .user(
                        User.builder()
                                .userName("Alibek.Aglanov")
                                .build()
                )
                .trainers(new ArrayList<>())
                .build();

        trainee.addTrainer(oldTrainer);

        when(traineeRepository.findByUsername("Alibek.Aglanov"))
                .thenReturn(Optional.of(trainee));

        List<TrainerSummaryResponse> result =
                traineeService.updateTrainersList(request);

        assertThat(trainee.getTrainers())
                .isEmpty();

        assertThat(result)
                .isEmpty();

        verifyNoInteractions(trainerRepository);
        verifyNoInteractions(trainerSummaryResponseMapper);
    }

    private Trainer createTrainer(Long id, String username) {
        User user = User.builder()
                .userName(username)
                .firstName("Trainer")
                .lastName("Test")
                .isActive(true)
                .build();

        return Trainer.builder()
                .id(id)
                .user(user)
                .trainees(new ArrayList<>())
                .build();
    }
}