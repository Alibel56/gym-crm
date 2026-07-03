package com.practice.dao;

import com.practice.entity.Trainer;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerRepositoryImpl implements TrainerRepository {

    private static final Logger log = LoggerFactory.getLogger(TrainerRepositoryImpl.class);

    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public Trainer save(Trainer trainer) {
        sessionFactory.getCurrentSession().persist(trainer);
        log.info("Persisted trainer id={}", trainer.getId());
        return trainer;
    }

    @Override
    public void update(Trainer trainer) {
        sessionFactory.getCurrentSession().merge(trainer);
        log.info("Merged trainer id={}", trainer.getId());
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        log.debug("Finding trainer by id={}", id);
        var t = Optional.ofNullable(sessionFactory.getCurrentSession().find(Trainer.class, id));
        log.debug("Found trainer by id={}", id);
        return t;
    }

    @Override
    public List<Trainer> findAll() {
        log.debug("Finding all trainers");
        var t = sessionFactory.getCurrentSession().createQuery("from Trainer", Trainer.class).list();
        log.debug("Found {} trainers", t.size());
        return t;
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        log.debug("Finding trainer by username={}", username);
        var t = Optional.ofNullable(sessionFactory.getCurrentSession().createQuery("""
                SELECT t
                FROM Trainer t
                left join fetch t.trainees
                WHERE t.user.userName = :username
                """, Trainer.class)
                .setParameter("username", username)
                .getSingleResultOrNull()
        );
        log.debug("Found trainer by username={}", username);
        return t;
    }

    @Override
    public List<Trainer> findAllNotAssignedByTraineeUserName(String username) {
        log.debug("Finding all trainers not assigned by trainee username={}", username);
        var t = sessionFactory.getCurrentSession().createQuery("""
            SELECT t FROM Trainer t
            WHERE t NOT IN (
                SELECT tr FROM Trainee tn JOIN tn.trainers tr WHERE tn.user.userName = :username
            )
        """, Trainer.class)
                .setParameter("username", username)
                .getResultList();
        log.debug("Found {} trainers not assigned by trainee username={}", t.size(), username);
        return t;
    }

    @Override
    public void switchActive(Long id) {
        log.debug("Switching active trainer id={}", id);
        var trainer = findById(id);
        if (trainer.isEmpty()) {
            log.error("Trainer not found id={}", id);
            throw new EntityNotFoundException();
        }
        trainer.get().getUser().setIsActive(!trainer.get().getUser().getIsActive());
        log.debug("Switched active trainer id={}", id);
    }

    @Override
    public boolean isActive(Long id) {
        log.debug("Checking if active status of trainer id={}", id);
        var trainer = findById(id);
        if (trainer.isEmpty()) {
            log.error("Trainer not found id={}", id);
            throw new EntityNotFoundException();
        }
        log.debug("Got active status for trainer id={}", id);
        return trainer.get().getUser().getIsActive();
    }

}
