package com.practice.dao;

import com.practice.entity.User;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                        select u
                        from User u
                        where u.userName = :username
                        """, User.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    @Override
    public boolean existsByUsername(String username) {
        Long count = sessionFactory.getCurrentSession()
                .createQuery("""
                        select count(u.id)
                        from User u
                        where u.userName = :username
                        """, Long.class)
                .setParameter("username", username)
                .uniqueResult();

        return count != null && count > 0;
    }

    @Override
    public List<User> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("select u from User u", User.class)
                .getResultList();
    }
}