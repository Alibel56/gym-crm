package com.practice.dao;

import com.practice.entity.Training;
import com.practice.utility.Criteria;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingRepositoryImpl implements TrainingRepository {

    private static final Logger log = LoggerFactory.getLogger(TrainingRepositoryImpl.class);
    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Training save(Training training) {
        sessionFactory.getCurrentSession().persist(training);
        log.info("Persisted training id={}", training.getId());
        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        log.info("Finding training by id={}", id);
        var t = Optional.ofNullable(sessionFactory.getCurrentSession().find(Training.class, id));
        log.info("Found training by id={}", id);
        return t;
    }

    @Override
    public List<Training> findAll() {
        log.info("Finding all trainings");
        var t = sessionFactory.getCurrentSession().createQuery("from Training", Training.class).list();
        log.info("Found {} trainings", t.size());
        return t;
    }

    @Override
    public List<Training> findAllTraineeTrainingsByCriteria(String username, Criteria criteria) {
        log.debug("Finding all trainings for trainee username={} with criteria={}", username, criteria);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Training> cq = cb.createQuery(Training.class);
        Root<Training> training = cq.from(Training.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(
                training.get("trainee")
                        .get("user")
                        .get("userName"),
                username));

        addEqualPredicate(
                cb,
                predicates,
                training.get("trainer").get("user").get("userName"),
                criteria.getTrainerName());

        addEqualPredicate(
                cb,
                predicates,
                training.get("trainingType").get("name"),
                criteria.getTrainingType());

        addGreaterOrEqualPredicate(
                cb,
                predicates,
                training.get("trainingDate"),
                criteria.getFromDate());

        addLessOrEqualPredicate(
                cb,
                predicates,
                training.get("trainingDate"),
                criteria.getToDate());

        cq.select(training)
                .where(predicates.toArray(new Predicate[0]));

        var result = session.createQuery(cq).getResultList();
        log.debug("Found {} trainings for trainee username={}", result.size(), username);
        return result;
    }

    @Override
    public List<Training> findAllTrainerTrainingsByCriteria(String username, Criteria criteria) {
        log.debug("Finding all trainings for trainer username={} with criteria={}", username, criteria);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Training> cq = cb.createQuery(Training.class);
        Root<Training> training = cq.from(Training.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(
                training.get("trainer")
                        .get("user")
                        .get("userName"),
                username));

        addEqualPredicate(
                cb,
                predicates,
                training.get("trainee").get("user").get("userName"),
                criteria.getTraineeName());

        addGreaterOrEqualPredicate(
                cb,
                predicates,
                training.get("trainingDate"),
                criteria.getFromDate());

        addLessOrEqualPredicate(
                cb,
                predicates,
                training.get("trainingDate"),
                criteria.getToDate());

        cq.select(training)
                .where(predicates.toArray(new Predicate[0]));

        var result = session.createQuery(cq).getResultList();
        log.debug("Found {} trainings for trainer username={}", result.size(), username);
        return result;
    }

    private <T> void addEqualPredicate(
            CriteriaBuilder cb,
            List<Predicate> predicates,
            Path<T> path,
            T value) {

        if (value != null) {
            log.debug("Adding equal predicate for path={}", path);
            predicates.add(cb.equal(path, value));
        }
    }

    private <T extends Comparable<? super T>> void addGreaterOrEqualPredicate(
            CriteriaBuilder cb,
            List<Predicate> predicates,
            Path<T> path,
            T value) {

        if (value != null) {
            log.debug("Adding greater or equal predicate for path={}", path);
            predicates.add(cb.greaterThanOrEqualTo(path, value));
        }
    }

    private <T extends Comparable<? super T>> void addLessOrEqualPredicate(
            CriteriaBuilder cb,
            List<Predicate> predicates,
            Path<T> path,
            T value) {

        if (value != null) {
            log.debug("Adding less or equal predicate for path={}", path);
            predicates.add(cb.lessThanOrEqualTo(path, value));
        }
    }
}
