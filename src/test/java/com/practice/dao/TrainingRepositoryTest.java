package com.practice.dao;

import com.practice.entity.Trainee;
import com.practice.entity.Trainer;
import com.practice.entity.Training;
import com.practice.entity.TrainingType;
import com.practice.entity.User;
import com.practice.utility.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrainingRepositoryTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    class SimpleMethods {

        @Mock
        private SessionFactory sessionFactory;

        @Mock
        private Session session;

        @Mock
        private Query<Training> query;

        private TrainingRepositoryImpl repository;

        private Training training;

        @BeforeEach
        void setUp() {
            repository = new TrainingRepositoryImpl();
            repository.setSessionFactory(sessionFactory);

            training = Training.builder()
                    .id(1L)
                    .trainingName("Cardio")
                    .trainingDate(LocalDate.of(2026, 1, 1))
                    .trainingDuration(60)
                    .build();

            lenient().when(sessionFactory.getCurrentSession()).thenReturn(session);
        }

        @Test
        void save_shouldPersistTrainingAndReturnIt() {
            Training result = repository.save(training);

            verify(session).persist(training);
            assertThat(result).isEqualTo(training);
        }

        @Test
        void findById_shouldReturnTraining_whenFound() {
            when(session.find(Training.class, 1L)).thenReturn(training);

            Optional<Training> result = repository.findById(1L);

            assertThat(result).isPresent().contains(training);
        }

        @Test
        void findById_shouldReturnEmpty_whenNotFound() {
            when(session.find(Training.class, 1L)).thenReturn(null);

            Optional<Training> result = repository.findById(1L);

            assertThat(result).isEmpty();
        }

        @Test
        void findAll_shouldReturnListOfTrainings() {
            when(session.createQuery("from Training", Training.class)).thenReturn(query);
            when(query.list()).thenReturn(List.of(training));

            List<Training> result = repository.findAll();

            assertThat(result).hasSize(1).containsExactly(training);
        }

        @Test
        void findAll_shouldReturnEmptyList_whenNoTrainings() {
            when(session.createQuery("from Training", Training.class)).thenReturn(query);
            when(query.list()).thenReturn(List.of());

            List<Training> result = repository.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class CriteriaMethods {

        private SessionFactory sessionFactory;
        private TrainingRepositoryImpl repository;

        @BeforeEach
        void setUp() {
            Configuration configuration = new Configuration();
            configuration.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
            configuration.setProperty("hibernate.connection.url", "jdbc:h2:mem:training_repo_test;DB_CLOSE_DELAY=-1");
            configuration.setProperty("hibernate.connection.username", "sa");
            configuration.setProperty("hibernate.connection.password", "");
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            configuration.setProperty("hibernate.current_session_context_class", "thread");

            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(Trainee.class);
            configuration.addAnnotatedClass(Trainer.class);
            configuration.addAnnotatedClass(Training.class);
            configuration.addAnnotatedClass(TrainingType.class);

            sessionFactory = configuration.buildSessionFactory();

            repository = new TrainingRepositoryImpl();
            repository.setSessionFactory(sessionFactory);

            seedData();
        }

        @AfterEach
        void tearDown() {
            sessionFactory.close();
        }

        private void seedData() {
            runInTransaction(session -> {
                TrainingType cardio = TrainingType.builder().name("Cardio").build();
                TrainingType yoga = TrainingType.builder().name("Yoga").build();
                session.persist(cardio);
                session.persist(yoga);

                User janeUser = User.builder()
                        .firstName("Jane").lastName("Doe").userName("jane.trainee")
                        .password("pass").isActive(true).build();
                User aliceUser = User.builder()
                        .firstName("Alice").lastName("Smith").userName("alice.trainee")
                        .password("pass").isActive(true).build();
                User johnUser = User.builder()
                        .firstName("John").lastName("Roe").userName("john.trainer")
                        .password("pass").isActive(true).build();
                User bobUser = User.builder()
                        .firstName("Bob").lastName("Fox").userName("bob.trainer")
                        .password("pass").isActive(true).build();
                session.persist(janeUser);
                session.persist(aliceUser);
                session.persist(johnUser);
                session.persist(bobUser);

                Trainee jane = Trainee.builder().user(janeUser).build();
                Trainee alice = Trainee.builder().user(aliceUser).build();
                session.persist(jane);
                session.persist(alice);

                Trainer john = Trainer.builder().user(johnUser).trainingType(cardio).build();
                Trainer bob = Trainer.builder().user(bobUser).trainingType(yoga).build();
                session.persist(john);
                session.persist(bob);

                Training janeCardioWithJohn = Training.builder()
                        .trainee(jane).trainer(john).trainingType(cardio)
                        .trainingName("Jane Cardio").trainingDate(LocalDate.of(2026, 1, 10))
                        .trainingDuration(60).build();
                Training janeYogaWithBob = Training.builder()
                        .trainee(jane).trainer(bob).trainingType(yoga)
                        .trainingName("Jane Yoga").trainingDate(LocalDate.of(2026, 3, 10))
                        .trainingDuration(45).build();
                Training aliceCardioWithJohn = Training.builder()
                        .trainee(alice).trainer(john).trainingType(cardio)
                        .trainingName("Alice Cardio").trainingDate(LocalDate.of(2026, 1, 15))
                        .trainingDuration(50).build();
                session.persist(janeCardioWithJohn);
                session.persist(janeYogaWithBob);
                session.persist(aliceCardioWithJohn);
            });
        }

        private void runInTransaction(Consumer<Session> work) {
            inTransaction(session -> {
                work.accept(session);
                return null;
            });
        }

        private <T> T inTransaction(Function<Session, T> work) {
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            try {
                T result = work.apply(session);
                session.getTransaction().commit();
                return result;
            } catch (RuntimeException e) {
                session.getTransaction().rollback();
                throw e;
            }
        }

        @Test
        void findAllTraineeTrainingsByCriteria_shouldReturnAllTraineeTrainings_whenNoFiltersGiven() {
            Criteria criteria = Criteria.builder().build();

            List<Training> result = inTransaction(session ->
                    repository.findAllTraineeTrainingsByCriteria("jane.trainee", criteria));

            assertThat(result)
                    .extracting(Training::getTrainingName)
                    .containsExactlyInAnyOrder("Jane Cardio", "Jane Yoga");
        }

        @Test
        void findAllTraineeTrainingsByCriteria_shouldReturnOnlyMatchingTraining_whenAllFiltersApplied() {
            Criteria criteria = Criteria.builder()
                    .trainerName("john.trainer")
                    .trainingType("Cardio")
                    .fromDate(LocalDate.of(2026, 1, 1))
                    .toDate(LocalDate.of(2026, 1, 31))
                    .build();

            List<Training> result = inTransaction(session ->
                    repository.findAllTraineeTrainingsByCriteria("jane.trainee", criteria));

            assertThat(result)
                    .extracting(Training::getTrainingName)
                    .containsExactly("Jane Cardio");
        }

        @Test
        void findAllTraineeTrainingsByCriteria_shouldExcludeTrainingsOutsideDateRange() {
            Criteria criteria = Criteria.builder()
                    .fromDate(LocalDate.of(2026, 2, 1))
                    .toDate(LocalDate.of(2026, 12, 31))
                    .build();

            List<Training> result = inTransaction(session ->
                    repository.findAllTraineeTrainingsByCriteria("jane.trainee", criteria));

            assertThat(result)
                    .extracting(Training::getTrainingName)
                    .containsExactly("Jane Yoga");
        }

        @Test
        void findAllTrainerTrainingsByCriteria_shouldReturnAllTrainerTrainings_whenNoFiltersGiven() {
            Criteria criteria = Criteria.builder().build();

            List<Training> result = inTransaction(session ->
                    repository.findAllTrainerTrainingsByCriteria("john.trainer", criteria));

            assertThat(result)
                    .extracting(Training::getTrainingName)
                    .containsExactlyInAnyOrder("Jane Cardio", "Alice Cardio");
        }

        @Test
        void findAllTrainerTrainingsByCriteria_shouldReturnOnlyMatchingTraining_whenTraineeNameFilterApplied() {
            Criteria criteria = Criteria.builder()
                    .traineeName("jane.trainee")
                    .build();

            List<Training> result = inTransaction(session ->
                    repository.findAllTrainerTrainingsByCriteria("john.trainer", criteria));

            assertThat(result)
                    .extracting(Training::getTrainingName)
                    .containsExactly("Jane Cardio");
        }

        @Test
        void findAllTrainerTrainingsByCriteria_shouldExcludeTrainingsOutsideDateRange() {
            Criteria criteria = Criteria.builder()
                    .fromDate(LocalDate.of(2026, 1, 12))
                    .toDate(LocalDate.of(2026, 1, 31))
                    .build();

            List<Training> result = inTransaction(session ->
                    repository.findAllTrainerTrainingsByCriteria("john.trainer", criteria));

            assertThat(result)
                    .extracting(Training::getTrainingName)
                    .containsExactly("Alice Cardio");
        }
    }
}