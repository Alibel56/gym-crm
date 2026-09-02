package com.practice.dao;

import com.practice.entity.Trainer;
import com.practice.entity.User;
import com.practice.testtags.EdgeCaseTest;
import com.practice.testtags.ExceptionTest;
import com.practice.testtags.NegativeTest;
import com.practice.testtags.PositiveTest;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerRepositoryTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<Trainer> query;

    private TrainerRepositoryImpl repository;

    private Trainer trainer;
    private User user;

    @BeforeEach
    void setUp() {
        repository = new TrainerRepositoryImpl();
        repository.setSessionFactory(sessionFactory);

        user = new User();
        user.setUserName("jane.smith");
        user.setIsActive(true);

        trainer = Trainer.builder()
                .id(1L)
                .user(user)
                .build();

        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    @PositiveTest
    void save_shouldPersistTrainerAndReturnIt() {
        Trainer result = repository.save(trainer);

        verify(session).persist(trainer);
        assertThat(result).isEqualTo(trainer);
    }

    @Test
    @PositiveTest
    void findById_shouldReturnTrainer_whenFound() {
        when(session.find(Trainer.class, 1L)).thenReturn(trainer);

        Optional<Trainer> result = repository.findById(1L);

        assertThat(result).isPresent().contains(trainer);
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void findById_shouldReturnEmpty_whenNotFound() {
        when(session.find(Trainer.class, 1L)).thenReturn(null);

        Optional<Trainer> result = repository.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @PositiveTest
    void findAll_shouldReturnListOfTrainers() {
        List<Trainer> trainers = List.of(trainer);
        when(session.createQuery("from Trainer", Trainer.class)).thenReturn(query);
        when(query.list()).thenReturn(trainers);

        List<Trainer> result = repository.findAll();

        assertThat(result).hasSize(1).containsExactly(trainer);
    }

    @Test
    @EdgeCaseTest
    void findAll_shouldReturnEmptyList_whenNoTrainers() {
        when(session.createQuery("from Trainer", Trainer.class)).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<Trainer> result = repository.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    @PositiveTest
    void findByUsername_shouldReturnTrainer_whenFound() {
        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.setParameter("username", "jane.smith")).thenReturn(query);
        when(query.getSingleResultOrNull()).thenReturn(trainer);

        Optional<Trainer> result = repository.findByUsername("jane.smith");

        assertThat(result).isPresent().contains(trainer);
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void findByUsername_shouldReturnEmpty_whenNotFound() {
        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.setParameter("username", "unknown")).thenReturn(query);
        when(query.getSingleResultOrNull()).thenReturn(null);

        Optional<Trainer> result = repository.findByUsername("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    @PositiveTest
    void findAllNotAssignedByTraineeUserName_shouldReturnUnassignedTrainers() {
        List<Trainer> unassigned = List.of(trainer);
        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.setParameter("username", "john.doe")).thenReturn(query);
        when(query.getResultList()).thenReturn(unassigned);

        List<Trainer> result = repository.findAllNotAssignedByTraineeUserName("john.doe");

        assertThat(result).hasSize(1).containsExactly(trainer);
    }

    @Test
    @EdgeCaseTest
    void findAllNotAssignedByTraineeUserName_shouldReturnEmptyList_whenAllTrainersAssigned() {
        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.setParameter("username", "john.doe")).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        List<Trainer> result = repository.findAllNotAssignedByTraineeUserName("john.doe");

        assertThat(result).isEmpty();
    }

    @Test
    @PositiveTest
    void switchActive_shouldToggleActiveStatus_whenFound() {
        when(session.find(Trainer.class, 1L)).thenReturn(trainer);
        boolean before = user.getIsActive();

        repository.switchActive(1L);

        assertThat(user.getIsActive()).isEqualTo(!before);
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void switchActive_shouldThrowException_whenNotFound() {
        when(session.find(Trainer.class, 1L)).thenReturn(null);

        assertThatThrownBy(() -> repository.switchActive(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @PositiveTest
    void isActive_shouldReturnTrue_whenTrainerIsActive() {
        when(session.find(Trainer.class, 1L)).thenReturn(trainer);

        boolean result = repository.isActive(1L);

        assertThat(result).isTrue();
    }

    @Test
    @PositiveTest
    void isActive_shouldReturnFalse_whenTrainerIsInactive() {
        user.setIsActive(false);
        when(session.find(Trainer.class, 1L)).thenReturn(trainer);

        boolean result = repository.isActive(1L);

        assertThat(result).isFalse();
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void isActive_shouldThrowException_whenNotFound() {
        when(session.find(Trainer.class, 1L)).thenReturn(null);

        assertThatThrownBy(() -> repository.isActive(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}

