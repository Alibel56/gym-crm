package com.practice.service;

import com.practice.dao.UserRepository;
import com.practice.dto.request.LoginRequest;
import com.practice.dto.request.UpdateLoginRequest;
import com.practice.entity.User;
import com.practice.exception.AuthenticationException;
import com.practice.utility.Credentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        userService.setUserRepository(userRepository);
    }

    @Test
    void generateCredentials_shouldGenerateUsernameAndPassword() {
        User existingUser = User.builder()
                .userName("Alibek.Aglanov")
                .password("password")
                .build();

        List<User> users = List.of(existingUser);

        when(userRepository.findAll()).thenReturn(users);

        Credentials credentials = userService.generateCredentials(
                "John",
                "Smith"
        );

        assertThat(credentials).isNotNull();
        assertThat(credentials.username()).isNotBlank();
        assertThat(credentials.password()).isNotBlank();

        verify(userRepository).findAll();
    }

    @Test
    void authenticate_shouldReturnUser_whenCredentialsAreCorrect() {
        LoginRequest request = new LoginRequest(
                "Alibek.Aglanov",
                "password123"
        );

        User user = User.builder()
                .userName("Alibek.Aglanov")
                .password("password123")
                .build();

        when(userRepository.findByUsername(request.userName()))
                .thenReturn(Optional.of(user));

        User result = userService.authenticate(request);

        assertThat(result).isSameAs(user);
        assertThat(result.getUserName()).isEqualTo("Alibek.Aglanov");
        assertThat(result.getPassword()).isEqualTo("password123");

        verify(userRepository).findByUsername("Alibek.Aglanov");
    }

    @Test
    void authenticate_shouldThrowAuthenticationException_whenUserNotFound() {
        LoginRequest request = new LoginRequest(
                "Unknown.User",
                "password123"
        );

        when(userRepository.findByUsername(request.userName()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.authenticate(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");

        verify(userRepository).findByUsername("Unknown.User");
    }

    @Test
    void authenticate_shouldThrowAuthenticationException_whenPasswordIsIncorrect() {
        LoginRequest request = new LoginRequest(
                "Alibek.Aglanov",
                "wrongPassword"
        );

        User user = User.builder()
                .userName("Alibek.Aglanov")
                .password("correctPassword")
                .build();

        when(userRepository.findByUsername(request.userName()))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.authenticate(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");

        verify(userRepository).findByUsername("Alibek.Aglanov");
    }

    @Test
    void changePassword_shouldChangePassword_whenOldPasswordIsCorrect() {
        UpdateLoginRequest request = new UpdateLoginRequest(
                "Alibek.Aglanov",
                "oldPassword",
                "newPassword"
        );

        User user = User.builder()
                .userName("Alibek.Aglanov")
                .password("oldPassword")
                .build();

        when(userRepository.findByUsername(request.userName()))
                .thenReturn(Optional.of(user));

        userService.changePassword(request);

        assertThat(user.getPassword()).isEqualTo("newPassword");

        verify(userRepository).findByUsername("Alibek.Aglanov");
    }

    @Test
    void changePassword_shouldThrowAuthenticationException_whenUserNotFound() {
        UpdateLoginRequest request = new UpdateLoginRequest(
                "Unknown.User",
                "oldPassword",
                "newPassword"
        );

        when(userRepository.findByUsername(request.userName()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");

        verify(userRepository).findByUsername("Unknown.User");
    }

    @Test
    void changePassword_shouldThrowAuthenticationException_whenOldPasswordIsIncorrect() {
        UpdateLoginRequest request = new UpdateLoginRequest(
                "Alibek.Aglanov",
                "wrongOldPassword",
                "newPassword"
        );

        User user = User.builder()
                .userName("Alibek.Aglanov")
                .password("correctOldPassword")
                .build();

        when(userRepository.findByUsername(request.userName()))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.changePassword(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");

        assertThat(user.getPassword()).isEqualTo("correctOldPassword");

        verify(userRepository).findByUsername("Alibek.Aglanov");
    }
}