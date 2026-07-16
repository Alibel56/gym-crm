package com.practice.service;

import com.practice.dao.UserRepository;
import com.practice.dto.request.LoginRequest;
import com.practice.dto.request.UpdateLoginRequest;
import com.practice.entity.User;
import com.practice.exception.AuthenticationException;
import com.practice.utility.Credentials;
import com.practice.utility.ServiceUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log =
            LoggerFactory.getLogger(UserService.class);

    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Credentials generateCredentials(
            String firstName,
            String lastName
    ) {
        String username = ServiceUtility.generateUserName(
                firstName,
                lastName,
                userRepository.findAll()
        );

        String password = ServiceUtility.generatePassword();

        return new Credentials(username, password);
    }

    public User authenticate(LoginRequest request) {
        User user = userRepository.findByUsername(request.userName())
                .orElseThrow(() ->
                        new AuthenticationException(
                                "Invalid username or password"
                        ));

        if (!user.getPassword().equals(request.password())) {
            throw new AuthenticationException(
                    "Invalid username or password"
            );
        }

        return user;
    }

    @Transactional
    public void changePassword(UpdateLoginRequest request) {
        User user = userRepository.findByUsername(request.userName())
                .orElseThrow(() ->
                        new AuthenticationException(
                                "Invalid username or password"
                        ));

        if (!user.getPassword().equals(request.oldPassword())) {
            throw new AuthenticationException(
                    "Invalid username or password"
            );
        }

        user.setPassword(request.newPassword());

        log.info(
                "Password changed for username={}",
                request.userName()
        );
    }
}
