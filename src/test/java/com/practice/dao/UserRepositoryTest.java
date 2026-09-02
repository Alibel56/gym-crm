package com.practice.dao;

import com.practice.entity.User;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<User> userQuery;

    @Mock
    private Query<Long> longQuery;

    private UserRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new UserRepositoryImpl();
        repository.setSessionFactory(sessionFactory);

        lenient().when(sessionFactory.getCurrentSession())
                .thenReturn(session);
    }

    @Test
    @PositiveTest
    void findByUsername_shouldReturnUser() {
        User user = User.builder()
                .id(1L)
                .userName("john.doe")
                .password("password")
                .build();

        when(session.createQuery(
                """
                select u
                from User u
                where u.userName = :username
                """,
                User.class))
                .thenReturn(userQuery);

        when(userQuery.setParameter("username", "john.doe"))
                .thenReturn(userQuery);

        when(userQuery.uniqueResultOptional())
                .thenReturn(Optional.of(user));

        Optional<User> result = repository.findByUsername("john.doe");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);

        verify(userQuery).setParameter("username", "john.doe");
        verify(userQuery).uniqueResultOptional();
    }

    @Test
    @EdgeCaseTest
    void findByUsername_shouldReturnEmptyOptional() {
        when(session.createQuery(
                """
                select u
                from User u
                where u.userName = :username
                """,
                User.class))
                .thenReturn(userQuery);

        when(userQuery.setParameter("username", "unknown"))
                .thenReturn(userQuery);

        when(userQuery.uniqueResultOptional())
                .thenReturn(Optional.empty());

        Optional<User> result = repository.findByUsername("unknown");

        assertThat(result).isEmpty();

        verify(userQuery).setParameter("username", "unknown");
        verify(userQuery).uniqueResultOptional();
    }

    @Test
    @PositiveTest
    void existsByUsername_shouldReturnTrue() {
        when(session.createQuery(
                """
                select count(u.id)
                from User u
                where u.userName = :username
                """,
                Long.class))
                .thenReturn(longQuery);

        when(longQuery.setParameter("username", "john.doe"))
                .thenReturn(longQuery);

        when(longQuery.uniqueResult())
                .thenReturn(1L);

        boolean exists = repository.existsByUsername("john.doe");

        assertThat(exists).isTrue();

        verify(longQuery).setParameter("username", "john.doe");
        verify(longQuery).uniqueResult();
    }

    @Test
    @EdgeCaseTest
    void existsByUsername_shouldReturnFalse_whenCountIsZero() {
        when(session.createQuery(
                """
                select count(u.id)
                from User u
                where u.userName = :username
                """,
                Long.class))
                .thenReturn(longQuery);

        when(longQuery.setParameter("username", "john.doe"))
                .thenReturn(longQuery);

        when(longQuery.uniqueResult())
                .thenReturn(0L);

        boolean exists = repository.existsByUsername("john.doe");

        assertThat(exists).isFalse();
    }

    @Test
    @EdgeCaseTest
    void existsByUsername_shouldReturnFalse_whenCountIsNull() {
        when(session.createQuery(
                """
                select count(u.id)
                from User u
                where u.userName = :username
                """,
                Long.class))
                .thenReturn(longQuery);

        when(longQuery.setParameter("username", "john.doe"))
                .thenReturn(longQuery);

        when(longQuery.uniqueResult())
                .thenReturn(null);

        boolean exists = repository.existsByUsername("john.doe");

        assertThat(exists).isFalse();
    }

    @Test
    @PositiveTest
    void findAll_shouldReturnUsers() {
        List<User> users = List.of(
                User.builder().id(1L).userName("user1").build(),
                User.builder().id(2L).userName("user2").build()
        );

        when(session.createQuery(
                "select u from User u",
                User.class))
                .thenReturn(userQuery);

        when(userQuery.getResultList())
                .thenReturn(users);

        List<User> result = repository.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactlyElementsOf(users);

        verify(userQuery).getResultList();
    }
}
