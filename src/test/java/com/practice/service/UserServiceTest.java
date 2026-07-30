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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GymMetrics gymMetrics;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private BruteForceProtectionService bruteForceProtectionService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private Authentication authentication;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        userService.setUserRepository(userRepository);
        userService.setGymMetrics(gymMetrics);
        userService.setPasswordEncoder(passwordEncoder);
        userService.setAuthenticationManager(authenticationManager);
        userService.setJwtService(jwtService);
        userService.setBruteForceProtectionService(bruteForceProtectionService);
        userService.setTokenBlacklistService(tokenBlacklistService);
    }

    @Test
    void encodePasswordShouldReturnEncodedPassword() {
        when(passwordEncoder.encode("password"))
                .thenReturn("encoded-password");

        String result = userService.encodePassword("password");

        assertThat(result).isEqualTo("encoded-password");

        verify(passwordEncoder).encode("password");
    }

    @Test
    void authenticateShouldReturnUserForValidCredentials() {
        LoginRequest request = mock(LoginRequest.class);

        User user = new User();
        user.setUserName("john");
        user.setPassword("encoded-password");

        when(request.userName()).thenReturn("john");
        when(request.password()).thenReturn("password");
        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded-password"))
                .thenReturn(true);

        User result = userService.authenticate(request);

        assertThat(result).isSameAs(user);
    }

    @Test
    void authenticateShouldThrowWhenUserNotFound() {
        LoginRequest request = mock(LoginRequest.class);

        when(request.userName()).thenReturn("unknown");
        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.authenticate(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
    }

    @Test
    void authenticateShouldThrowWhenPasswordIsIncorrect() {
        LoginRequest request = mock(LoginRequest.class);

        User user = new User();
        user.setPassword("encoded-password");

        when(request.userName()).thenReturn("john");
        when(request.password()).thenReturn("wrong-password");
        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(
                "wrong-password",
                "encoded-password"
        )).thenReturn(false);

        assertThatThrownBy(() -> userService.authenticate(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void loginShouldReturnJwtTokenForValidCredentials() {
        LoginRequest request = mock(LoginRequest.class);
        UserDetails principal = mock(UserDetails.class);

        when(request.userName()).thenReturn("john");
        when(request.password()).thenReturn("password");

        when(bruteForceProtectionService.isBlocked("john"))
                .thenReturn(false);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(principal);

        when(jwtService.generateToken(principal))
                .thenReturn("jwt-token");

        when(jwtService.getExpirationSeconds())
                .thenReturn(3600L);

        LoginResponse response = userService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(3600L);

        verify(bruteForceProtectionService)
                .loginSucceeded("john");
        verify(gymMetrics)
                .recordAuthenticationAttempt(true);
    }

    @Test
    void loginShouldThrowWhenUserIsBlocked() {
        LoginRequest request = mock(LoginRequest.class);

        when(request.userName()).thenReturn("john");
        when(bruteForceProtectionService.isBlocked("john"))
                .thenReturn(true);
        when(bruteForceProtectionService.remainingBlockSeconds("john"))
                .thenReturn(120L);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("User is blocked. Try again in 120 seconds");

        verifyNoInteractions(authenticationManager);
        verify(gymMetrics, never())
                .recordAuthenticationAttempt(anyBoolean());
    }

    @Test
    void loginShouldRecordFailedAttemptWhenAuthenticationFails() {
        LoginRequest request = mock(LoginRequest.class);

        when(request.userName()).thenReturn("john");
        when(request.password()).thenReturn("wrong-password");
        when(bruteForceProtectionService.isBlocked("john"))
                .thenReturn(false);

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");

        verify(bruteForceProtectionService)
                .loginFailed("john");
        verify(gymMetrics)
                .recordAuthenticationAttempt(false);
        verify(jwtService, never())
                .generateToken(any());
    }

    @Test
    void changePasswordShouldEncodeAndSetNewPassword() {
        UpdateLoginRequest request = mock(UpdateLoginRequest.class);

        User user = new User();
        user.setPassword("old-encoded-password");

        when(request.userName()).thenReturn("john");
        when(request.oldPassword()).thenReturn("old-password");
        when(request.newPassword()).thenReturn("new-password");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "old-password",
                "old-encoded-password"
        )).thenReturn(true);

        when(passwordEncoder.encode("new-password"))
                .thenReturn("new-encoded-password");

        userService.changePassword(request);

        assertThat(user.getPassword())
                .isEqualTo("new-encoded-password");

        verify(passwordEncoder).encode("new-password");
    }

    @Test
    void changePasswordShouldThrowWhenOldPasswordIsIncorrect() {
        UpdateLoginRequest request = mock(UpdateLoginRequest.class);

        User user = new User();
        user.setPassword("old-encoded-password");

        when(request.userName()).thenReturn("john");
        when(request.oldPassword()).thenReturn("wrong-password");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "old-encoded-password"
        )).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void logoutShouldRevokeToken() {
        String authorizationHeader = "Bearer jwt-token";
        Date expiration = Date.from(
                Instant.now().plusSeconds(3600)
        );

        when(jwtService.extractTokenId("jwt-token"))
                .thenReturn("token-id");
        when(jwtService.extractExpiration("jwt-token"))
                .thenReturn(expiration);

        userService.logout(authorizationHeader);

        verify(tokenBlacklistService)
                .revoke("token-id", expiration);
    }

    @Test
    void logoutShouldThrowWhenHeaderIsMissing() {
        assertThatThrownBy(() -> userService.logout(null))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage(
                        "Missing or malformed Authorization header"
                );

        verifyNoInteractions(jwtService, tokenBlacklistService);
    }

    @Test
    void logoutShouldThrowWhenHeaderIsMalformed() {
        assertThatThrownBy(() ->
                userService.logout("Basic credentials")
        )
                .isInstanceOf(AuthenticationException.class)
                .hasMessage(
                        "Missing or malformed Authorization header"
                );

        verifyNoInteractions(jwtService, tokenBlacklistService);
    }

    @Test
    void generateCredentialsShouldGenerateUsernameAndPassword() {
        User existingUser = new User();
        existingUser.setUserName("john.smith");

        when(userRepository.findAll())
                .thenReturn(List.of(existingUser));

        Credentials credentials =
                userService.generateCredentials("John", "Smith");

        assertThat(credentials.username()).isNotBlank();
        assertThat(credentials.password()).isNotBlank();

        verify(userRepository).findAll();
    }
}