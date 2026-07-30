package com.practice.service;

import com.practice.dao.UserRepository;
import com.practice.dto.request.LoginRequest;
import com.practice.dto.request.UpdateLoginRequest;
import com.practice.dto.response.LoginResponse;
import com.practice.entity.User;
import com.practice.exception.AuthenticationException;
import com.practice.metrics.GymMetrics;
import com.practice.security.BruteForceProtectionService;
import com.practice.security.JwtService;
import com.practice.security.TokenBlacklistService;
import com.practice.utility.Credentials;
import com.practice.utility.ServiceUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private UserRepository userRepository;
    private GymMetrics gymMetrics;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private BruteForceProtectionService bruteForceProtectionService;
    private TokenBlacklistService tokenBlacklistService;

    @Autowired public void setUserRepository(UserRepository v) { this.userRepository = v; }
    @Autowired public void setGymMetrics(GymMetrics v) { this.gymMetrics = v; }
    @Autowired public void setPasswordEncoder(PasswordEncoder v) { this.passwordEncoder = v; }
    @Autowired public void setAuthenticationManager(AuthenticationManager v) { this.authenticationManager = v; }
    @Autowired public void setJwtService(JwtService v) { this.jwtService = v; }
    @Autowired public void setBruteForceProtectionService(BruteForceProtectionService v) { this.bruteForceProtectionService = v; }
    @Autowired public void setTokenBlacklistService(TokenBlacklistService v) { this.tokenBlacklistService = v; }

    public Credentials generateCredentials(String firstName, String lastName) {
        String username = ServiceUtility.generateUserName(firstName, lastName, userRepository.findAll());
        return new Credentials(username, ServiceUtility.generatePassword());
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public User authenticate(LoginRequest request) {
        User user = userRepository.findByUsername(request.userName())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }
        return user;
    }

    public LoginResponse login(LoginRequest request) {
        if (bruteForceProtectionService.isBlocked(request.userName())) {
            long seconds = bruteForceProtectionService.remainingBlockSeconds(request.userName());
            throw new AuthenticationException("User is blocked. Try again in " + seconds + " seconds");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.userName(), request.password()));
            bruteForceProtectionService.loginSucceeded(request.userName());
            gymMetrics.recordAuthenticationAttempt(true);
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            return new LoginResponse(jwtService.generateToken(principal), "Bearer", jwtService.getExpirationSeconds());
        } catch (org.springframework.security.core.AuthenticationException ex) {
            bruteForceProtectionService.loginFailed(request.userName());
            gymMetrics.recordAuthenticationAttempt(false);
            log.warn("Authentication failed for username={}", request.userName());
            throw new AuthenticationException("Invalid username or password");
        }
    }

    @Transactional
    public void changePassword(UpdateLoginRequest request) {
        User user = userRepository.findByUsername(request.userName())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        log.info("Password changed for username={}", request.userName());
    }

    public void logout(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new AuthenticationException("Missing or malformed Authorization header");
        }
        String token = bearerToken.substring(7).trim();
        tokenBlacklistService.revoke(jwtService.extractTokenId(token), jwtService.extractExpiration(token));
    }
}
