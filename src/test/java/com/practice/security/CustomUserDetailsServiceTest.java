package com.practice.security;

import com.practice.dao.UserRepository;
import com.practice.entity.User;
import com.practice.testtags.ExceptionTest;
import com.practice.testtags.NegativeTest;
import com.practice.testtags.PositiveTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(userRepository);
    }

    @Test
    @PositiveTest
    void loadUserByUsernameShouldReturnUserDetails() {
        User user = new User();
        user.setUserName("john");
        user.setPassword("encodedPassword");
        user.setIsActive(true);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        UserDetails userDetails = service.loadUserByUsername("john");

        assertThat(userDetails.getUsername()).isEqualTo("john");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");

        verify(userRepository).findByUsername("john");
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void loadUserByUsernameShouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("john"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByUsername("john");
    }
}
