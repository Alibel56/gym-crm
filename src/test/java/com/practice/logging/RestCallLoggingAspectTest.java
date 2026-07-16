package com.practice.logging;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestCallLoggingAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    private RestCallLoggingAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new RestCallLoggingAspect();

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("TestController.test()");
    }

    @Test
    void logRestCall_shouldReturnResult() throws Throwable {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request));

        when(joinPoint.getArgs()).thenReturn(new Object[]{"abc", 123});
        when(joinPoint.proceed()).thenReturn("SUCCESS");

        Object result = aspect.logRestCall(joinPoint);

        assertThat(result).isEqualTo("SUCCESS");

        verify(joinPoint).proceed();
    }

    @Test
    void logRestCall_shouldRethrowException() throws Throwable {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request));

        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        when(joinPoint.proceed())
                .thenThrow(new IllegalArgumentException("boom"));

        assertThatThrownBy(() ->
                aspect.logRestCall(joinPoint))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("boom");
    }

    @Test
    void logRestCall_shouldIgnoreHttpServletRequestArgument() throws Throwable {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("PUT");

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request));

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);

        when(joinPoint.getArgs())
                .thenReturn(new Object[]{
                        servletRequest,
                        "body"
                });

        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.logRestCall(joinPoint);

        assertThat(result).isEqualTo("OK");
    }

    @Test
    void logRestCall_shouldWorkWithoutRequestContext() throws Throwable {

        RequestContextHolder.resetRequestAttributes();

        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = aspect.logRestCall(joinPoint);

        assertThat(result).isEqualTo("OK");
    }
}