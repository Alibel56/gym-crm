package com.practice.dao;

import com.practice.entity.Trainee;
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
class TraineeRepositoryTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<Trainee> query;

    private TraineeRepositoryImpl repository;

    private Trainee trainee;
    private User user;

    @BeforeEach
    void setUp() {
        repository = new TraineeRepositoryImpl();
        repository.setSessionFactory(sessionFactory);

        user = new User();
        user.setUserName("john.doe");
        user.setIsActive(true);

        trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUser(user);

        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    @PositiveTest
    void save_shouldPersistTraineeAndReturnIt() {
        Trainee result = repository.save(trainee);

        verify(session).persist(trainee);
        assertThat(result).isEqualTo(trainee);
    }

    @Test
    @PositiveTest
    void findById_shouldReturnTrainee_whenFound() {
        when(session.find(Trainee.class, 1L)).thenReturn(trainee);

        Optional<Trainee> result = repository.findById(1L);

        assertThat(result).isPresent().contains(trainee);
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void findById_shouldReturnEmpty_whenNotFound() {
        when(session.find(Trainee.class, 1L)).thenReturn(null);

        Optional<Trainee> result = repository.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @PositiveTest
    void findByUsername_shouldReturnTrainee_whenFound() {
        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.setParameter("username", "john.doe")).thenReturn(query);
        when(query.getSingleResultOrNull()).thenReturn(trainee);

        Optional<Trainee> result = repository.findByUsername("john.doe");

        assertThat(result).isPresent().contains(trainee);
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void findByUsername_shouldReturnEmpty_whenNotFound() {
        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.setParameter("username", "unknown")).thenReturn(query);
        when(query.getSingleResultOrNull()).thenReturn(null);

        Optional<Trainee> result = repository.findByUsername("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    @PositiveTest
    void findAll_shouldReturnListOfTrainees() {
        List<Trainee> trainees = List.of(trainee);
        when(session.createQuery("from Trainee", Trainee.class)).thenReturn(query);
        when(query.list()).thenReturn(trainees);

        List<Trainee> result = repository.findAll();

        assertThat(result).hasSize(1).containsExactly(trainee);
    }

    @Test
    @EdgeCaseTest
    void findAll_shouldReturnEmptyList_whenNoTrainees() {
        when(session.createQuery("from Trainee", Trainee.class)).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<Trainee> result = repository.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    @PositiveTest
    void delete_shouldRemoveTraineeAndReturnTrue_whenFound() {
        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.setParameter("username", "john.doe")).thenReturn(query);
        when(query.getSingleResultOrNull()).thenReturn(trainee);

        boolean result = repository.delete("john.doe");

        verify(session).remove(trainee);
        assertThat(result).isTrue();
    }

    @Test
    @NegativeTest
    void delete_shouldReturnFalseAndNotRemove_whenNotFound() {
        when(session.createQuery(anyString(), eq(Trainee.class))).thenReturn(query);
        when(query.setParameter("username", "unknown")).thenReturn(query);
        when(query.getSingleResultOrNull()).thenReturn(null);

        boolean result = repository.delete("unknown");

        verify(session, never()).remove(any());
        assertThat(result).isFalse();
    }

    @Test
    @PositiveTest
    void isActive_shouldReturnTrue_whenTraineeIsActive() {
        when(session.find(Trainee.class, 1L)).thenReturn(trainee);

        boolean result = repository.isActive(1L);

        assertThat(result).isTrue();
    }

    @Test
    @PositiveTest
    void isActive_shouldReturnFalse_whenTraineeIsInactive() {
        user.setIsActive(false);
        when(session.find(Trainee.class, 1L)).thenReturn(trainee);

        boolean result = repository.isActive(1L);

        assertThat(result).isFalse();
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void isActive_shouldThrowException_whenNotFound() {
        when(session.find(Trainee.class, 1L)).thenReturn(null);

        assertThatThrownBy(() -> repository.isActive(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
