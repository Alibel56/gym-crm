package com.practice.dao;

import com.practice.entity.Trainee;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeRepositoryImpl implements TraineeRepository {

    private static final Logger log = LoggerFactory.getLogger(TraineeRepositoryImpl.class);
    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Trainee save(Trainee trainee) {
        sessionFactory.getCurrentSession().persist(trainee);
        log.info("Persisted trainee id={}", trainee.getId());
        return trainee;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        log.debug("Finding trainee by id={}", id);
        var t = Optional.ofNullable(sessionFactory.getCurrentSession().find(Trainee.class, id));
        log.debug("Found trainee by id={}", id);
        return t;
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        log.debug("Finding trainee by username={}", username);
        var t = Optional.ofNullable(sessionFactory.getCurrentSession().createQuery("""
            SELECT t
            FROM Trainee t
            LEFT JOIN FETCH t.trainers
            WHERE t.user.userName = :username
            """, Trainee.class)
                .setParameter("username", username)
                .getSingleResultOrNull()
        );
        log.debug("Found trainee by username={}", username);
        return t;
    }

    @Override
    public List<Trainee> findAll() {
        log.debug("Finding all trainees");
        var t =  sessionFactory.getCurrentSession().createQuery("from Trainee", Trainee.class).list();
        log.debug("Found {} trainees", t.size());
        return t;
    }


    @Override
    public boolean delete(String username) {
        log.debug("Deleting trainee by username={}", username);
        var trainee = findByUsername(username);
        if (trainee.isEmpty()) {
            log.debug("Delete skipped, trainee not found username={}", username);
            return false;
        }
        sessionFactory.getCurrentSession().remove(trainee.get());
        log.debug("Deleted trainee by username={}", username);
        return true;
    }

    @Override
    public boolean isActive(Long id) {
        log.debug("Checking if active status of trainee id={}", id);
        var trainee = findById(id);
        if (trainee.isEmpty()) {
            log.error("Trainee not found id={}", id);
            throw new EntityNotFoundException();
        }
        log.debug("Got active status for trainee id={}", id);
        return trainee.get().getUser().getIsActive();
    }
}
