package com.practice.metrics;

import com.practice.dao.TraineeRepository;
import com.practice.entity.Trainee;
import com.practice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymMetricsQueryServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    private GymMetricsQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new GymMetricsQueryService(
                traineeRepository
        );
    }

    @Test
    void countActiveTraineesShouldReturnNumberOfActiveTrainees() {
        Trainee activeTrainee1 = createTrainee(true);
        Trainee activeTrainee2 = createTrainee(true);
        Trainee inactiveTrainee = createTrainee(false);

        when(traineeRepository.findAll()).thenReturn(
                List.of(
                        activeTrainee1,
                        activeTrainee2,
                        inactiveTrainee
                )
        );

        double result = queryService.countActiveTrainees();

        assertThat(result).isEqualTo(2.0);

        verify(traineeRepository).findAll();
    }

    @Test
    void countActiveTraineesShouldIgnoreInactiveTrainees() {
        Trainee inactiveTrainee1 = createTrainee(false);
        Trainee inactiveTrainee2 = createTrainee(false);

        when(traineeRepository.findAll()).thenReturn(
                List.of(
                        inactiveTrainee1,
                        inactiveTrainee2
                )
        );

        double result = queryService.countActiveTrainees();

        assertThat(result).isZero();

        verify(traineeRepository).findAll();
    }

    @Test
    void countActiveTraineesShouldIgnoreTraineeWithNullUser() {
        Trainee traineeWithoutUser = new Trainee();
        Trainee activeTrainee = createTrainee(true);

        when(traineeRepository.findAll()).thenReturn(
                List.of(
                        traineeWithoutUser,
                        activeTrainee
                )
        );

        double result = queryService.countActiveTrainees();

        assertThat(result).isEqualTo(1.0);

        verify(traineeRepository).findAll();
    }

    @Test
    void countActiveTraineesShouldIgnoreTraineeWithNullActiveStatus() {
        Trainee trainee = createTrainee(null);
        Trainee activeTrainee = createTrainee(true);

        when(traineeRepository.findAll()).thenReturn(
                List.of(
                        trainee,
                        activeTrainee
                )
        );

        double result = queryService.countActiveTrainees();

        assertThat(result).isEqualTo(1.0);

        verify(traineeRepository).findAll();
    }

    @Test
    void countActiveTraineesShouldReturnZeroWhenRepositoryIsEmpty() {
        when(traineeRepository.findAll()).thenReturn(List.of());

        double result = queryService.countActiveTrainees();

        assertThat(result).isZero();

        verify(traineeRepository).findAll();
    }

    private Trainee createTrainee(Boolean isActive) {
        User user = new User();
        user.setIsActive(isActive);

        Trainee trainee = new Trainee();
        trainee.setUser(user);

        return trainee;
    }
}