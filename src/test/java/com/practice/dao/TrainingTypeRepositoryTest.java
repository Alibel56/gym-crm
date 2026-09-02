package com.practice.dao;

import com.practice.entity.TrainingType;
import com.practice.testtags.EdgeCaseTest;
import com.practice.testtags.PositiveTest;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeRepositoryTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<TrainingType> query;

    private TrainingTypeRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new TrainingTypeRepositoryImpl();
        repository.setSessionFactory(sessionFactory);

        lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    @PositiveTest
    void findById_shouldReturnTrainingType_whenExists() {
        TrainingType cardio = TrainingType.builder()
                .id(1L)
                .name("Cardio")
                .build();

        when(session.createQuery(
                "from TrainingType where id = :id",
                TrainingType.class))
                .thenReturn(query);

        when(query.setParameter("id", 1L)).thenReturn(query);
        when(query.uniqueResult()).thenReturn(cardio);

        Optional<TrainingType> result = repository.findById(1L);

        assertThat(result)
                .isPresent()
                .contains(cardio);
    }

    @Test
    @EdgeCaseTest
    void findById_shouldReturnEmptyOptional_whenNotExists() {
        when(session.createQuery(
                "from TrainingType where id = :id",
                TrainingType.class))
                .thenReturn(query);

        when(query.setParameter("id", 1L)).thenReturn(query);
        when(query.uniqueResult()).thenReturn(null);

        Optional<TrainingType> result = repository.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @PositiveTest
    void findAll_shouldReturnListOfTrainingTypes() {
        TrainingType cardio = TrainingType.builder()
                .id(1L)
                .name("Cardio")
                .build();

        TrainingType yoga = TrainingType.builder()
                .id(2L)
                .name("Yoga")
                .build();

        when(session.createQuery("from TrainingType", TrainingType.class))
                .thenReturn(query);

        when(query.list()).thenReturn(List.of(cardio, yoga));

        List<TrainingType> result = repository.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactly(cardio, yoga);
    }

    @Test
    @EdgeCaseTest
    void findAll_shouldReturnEmptyList_whenNoTrainingTypes() {
        when(session.createQuery("from TrainingType", TrainingType.class))
                .thenReturn(query);

        when(query.list()).thenReturn(List.of());

        List<TrainingType> result = repository.findAll();

        assertThat(result).isEmpty();
    }
}
