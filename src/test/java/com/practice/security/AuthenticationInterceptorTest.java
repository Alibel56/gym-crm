package com.practice.security;

import com.practice.dto.request.LoginRequest;
import com.practice.exception.AuthenticationException;
import com.practice.service.UserService;
import com.practice.testtags.EdgeCaseTest;
import com.practice.testtags.ExceptionTest;
import com.practice.testtags.NegativeTest;
import com.practice.testtags.PositiveTest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationInterceptorTest {

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private AuthenticationInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new AuthenticationInterceptor();
        interceptor.setUserService(userService);
    }

    @Test
    @PositiveTest
    void preHandle_shouldAuthenticateAndReturnTrue_whenCredentialsAreValid() {
        String header = createBasicHeader("john.doe", "password123");

        when(request.getHeader("Authorization")).thenReturn(header);

        boolean result = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        assertThat(result).isTrue();

        ArgumentCaptor<LoginRequest> captor =
                ArgumentCaptor.forClass(LoginRequest.class);

        verify(userService).authenticate(captor.capture());

        LoginRequest credentials = captor.getValue();

        assertThat(credentials.userName()).isEqualTo("john.doe");
        assertThat(credentials.password()).isEqualTo("password123");
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void preHandle_shouldPropagateAuthenticationException_whenCredentialsAreInvalid() {
        String header = createBasicHeader("john.doe", "wrongPassword");

        when(request.getHeader("Authorization")).thenReturn(header);

        doThrow(new AuthenticationException("Invalid username or password"))
                .when(userService)
                .authenticate(any(LoginRequest.class));

        assertThatThrownBy(() ->
                interceptor.preHandle(request, response, new Object())
        )
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");

        verify(userService).authenticate(any(LoginRequest.class));
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    @ExceptionTest
    void preHandle_shouldThrowException_whenAuthorizationHeaderIsMissing() {
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThatThrownBy(() ->
                interceptor.preHandle(request, response, new Object())
        )
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Missing or malformed Authorization header");

        verifyNoInteractions(userService);
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void preHandle_shouldThrowException_whenAuthorizationHeaderHasWrongPrefix() {
        when(request.getHeader("Authorization"))
                .thenReturn("Bearer some-token");

        assertThatThrownBy(() ->
                interceptor.preHandle(request, response, new Object())
        )
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Missing or malformed Authorization header");

        verifyNoInteractions(userService);
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    @ExceptionTest
    void preHandle_shouldThrowException_whenEncodedCredentialsAreEmpty() {
        when(request.getHeader("Authorization"))
                .thenReturn("Basic ");

        assertThatThrownBy(() ->
                interceptor.preHandle(request, response, new Object())
        )
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Missing or malformed Authorization header");

        verifyNoInteractions(userService);
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void preHandle_shouldThrowException_whenBase64IsInvalid() {
        when(request.getHeader("Authorization"))
                .thenReturn("Basic invalid-base64%%%");

        assertThatThrownBy(() ->
                interceptor.preHandle(request, response, new Object())
        )
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Malformed Authorization header");

        verifyNoInteractions(userService);
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void preHandle_shouldThrowException_whenDecodedCredentialsHaveNoSeparator() {
        String encoded = Base64.getEncoder()
                .encodeToString(
                        "john.doe-password123"
                                .getBytes(StandardCharsets.UTF_8)
                );

        when(request.getHeader("Authorization"))
                .thenReturn("Basic " + encoded);

        assertThatThrownBy(() ->
                interceptor.preHandle(request, response, new Object())
        )
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Malformed Authorization header");

        verifyNoInteractions(userService);
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    @ExceptionTest
    void preHandle_shouldThrowException_whenUsernameIsEmpty() {
        String header = createBasicHeader("", "password123");

        when(request.getHeader("Authorization")).thenReturn(header);

        assertThatThrownBy(() ->
                interceptor.preHandle(request, response, new Object())
        )
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Malformed Authorization header");

        verifyNoInteractions(userService);
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    @ExceptionTest
    void preHandle_shouldThrowException_whenPasswordIsEmpty() {
        String header = createBasicHeader("john.doe", "");

        when(request.getHeader("Authorization")).thenReturn(header);

        assertThatThrownBy(() ->
                interceptor.preHandle(request, response, new Object())
        )
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Username and password must not be blank");

        verifyNoInteractions(userService);
    }

    @Test
    @PositiveTest
    void preHandle_shouldCorrectlyParsePasswordContainingColon() {
        String header = createBasicHeader(
                "john.doe",
                "password:with:colon"
        );

        when(request.getHeader("Authorization")).thenReturn(header);

        boolean result = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        assertThat(result).isTrue();

        ArgumentCaptor<LoginRequest> captor =
                ArgumentCaptor.forClass(LoginRequest.class);

        verify(userService).authenticate(captor.capture());

        assertThat(captor.getValue().userName())
                .isEqualTo("john.doe");

        assertThat(captor.getValue().password())
                .isEqualTo("password:with:colon");
    }

    private String createBasicHeader(
            String username,
            String password
    ) {
        String credentials = username + ":" + password;

        String encoded = Base64.getEncoder()
                .encodeToString(
                        credentials.getBytes(StandardCharsets.UTF_8)
                );

        return "Basic " + encoded;
    }
}
