package com.practice.service;

import com.practice.dao.TraineeDao;
import com.practice.entity.Trainee;
import com.practice.utility.ServiceUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void create_ShouldGenerateCredentialsAndSaveTrainee() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Doe");

        Trainee saved = new Trainee();
        saved.setId(1L);

        try (MockedStatic<ServiceUtility> utility =
                     Mockito.mockStatic(ServiceUtility.class)) {

            utility.when(() ->
                            ServiceUtility.generateUserName(anyString(), anyString(), anyList()))
                    .thenReturn("john.doe");

            utility.when(ServiceUtility::generatePassword)
                    .thenReturn("password");

            when(traineeDao.findAll()).thenReturn(List.of());
            when(traineeDao.create(any(Trainee.class))).thenReturn(saved);

            Long result = traineeService.create(trainee);

            assertEquals(1L, result);
            assertEquals("john.doe", trainee.getUserName());
            assertEquals("password", trainee.getPassword());

            verify(traineeDao).create(trainee);
        }
    }

    @Test
    void update_WhenTraineeExists_ShouldReturnTrue() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);

        when(traineeDao.findById(1L))
                .thenReturn(Optional.of(new Trainee()));

        boolean result = traineeService.update(trainee);

        assertTrue(result);
        verify(traineeDao).update(trainee);
    }

    @Test
    void update_WhenTraineeDoesNotExist_ShouldReturnFalse() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);

        when(traineeDao.findById(1L))
                .thenReturn(Optional.empty());

        boolean result = traineeService.update(trainee);

        assertFalse(result);
        verify(traineeDao, never()).update(any());
    }

    @Test
    void delete_WhenTraineeExists_ShouldReturnTrue() {
        when(traineeDao.findById(1L))
                .thenReturn(Optional.of(new Trainee()));

        boolean result = traineeService.delete(1L);

        assertTrue(result);
        verify(traineeDao).delete(1L);
    }

    @Test
    void delete_WhenTraineeDoesNotExist_ShouldReturnFalse() {
        when(traineeDao.findById(1L))
                .thenReturn(Optional.empty());

        boolean result = traineeService.delete(1L);

        assertFalse(result);
        verify(traineeDao, never()).delete(anyLong());
    }

    @Test
    void findById_WhenTraineeExists_ShouldReturnTrainee() {
        Trainee trainee = new Trainee();

        when(traineeDao.findById(1L))
                .thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void findById_WhenTraineeDoesNotExist_ShouldReturnEmptyOptional() {
        when(traineeDao.findById(1L))
                .thenReturn(Optional.empty());

        Optional<Trainee> result = traineeService.findById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_WhenTraineesExist_ShouldReturnList() {
        List<Trainee> trainees = List.of(new Trainee(), new Trainee());

        when(traineeDao.findAll()).thenReturn(trainees);

        List<Trainee> result = traineeService.findAll();

        assertEquals(2, result.size());
        assertEquals(trainees, result);
    }

    @Test
    void findAll_WhenNoTraineesExist_ShouldReturnEmptyList() {
        when(traineeDao.findAll()).thenReturn(List.of());

        List<Trainee> result = traineeService.findAll();

        assertTrue(result.isEmpty());
    }
}
