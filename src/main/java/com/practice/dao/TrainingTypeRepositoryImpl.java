package com.practice.dao;


import com.practice.entity.TrainingType;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeRepositoryImpl implements TrainingTypeRepository {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeRepositoryImpl.class);
    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public Optional<TrainingType> findById(Long id) {
        log.info("Finding training type by id");
        var t = sessionFactory.getCurrentSession().createQuery("from TrainingType where id = :id", TrainingType.class)
                .setParameter("id", id)
                .uniqueResult();
        log.info("Found training type");
        return Optional.ofNullable(t);
    }

    @Override
    public List<TrainingType> findAll() {
        log.info("Finding all training types");
        var t = sessionFactory.getCurrentSession().createQuery("from TrainingType", TrainingType.class).list();
        log.info("Found {} trainings", t.size());
        return t;
    }

    @Override
    public void save(TrainingType fitness) {
        sessionFactory.getCurrentSession().persist(fitness);
        log.info("Persisted trainingType id={}", fitness.getId());
    }
}
