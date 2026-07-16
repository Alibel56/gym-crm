package com.practice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.security.AuthenticationInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WebConfigTest {

    private WebConfig webConfig;
    private AuthenticationInterceptor authenticationInterceptor;

    @BeforeEach
    void setUp() {
        webConfig = new WebConfig();
        authenticationInterceptor = mock(AuthenticationInterceptor.class);
        webConfig.setAuthenticationInterceptor(authenticationInterceptor);
    }

    @Test
    void shouldAddAuthenticationInterceptor() {
        InterceptorRegistry registry = mock(InterceptorRegistry.class);
        InterceptorRegistration registration = mock(InterceptorRegistration.class);

        when(registry.addInterceptor(authenticationInterceptor))
                .thenReturn(registration);

        when(registration.addPathPatterns(any(String[].class)))
                .thenReturn(registration);

        when(registration.excludePathPatterns(any(String[].class)))
                .thenReturn(registration);

        webConfig.addInterceptors(registry);

        verify(registry).addInterceptor(authenticationInterceptor);

        verify(registration).addPathPatterns("/api/**");

        verify(registration).excludePathPatterns(
                "/api/trainees",
                "/api/trainers",
                "/api/login"
        );
    }

    @Test
    void shouldAddJacksonConverterAsFirstConverter() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        webConfig.extendMessageConverters(converters);

        assertThat(converters)
                .hasSize(1);

        assertThat(converters.get(0))
                .isInstanceOf(MappingJackson2HttpMessageConverter.class);
    }

    @Test
    void shouldRegisterJavaTimeModule() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        webConfig.extendMessageConverters(converters);

        MappingJackson2HttpMessageConverter converter =
                (MappingJackson2HttpMessageConverter) converters.get(0);

        ObjectMapper mapper = converter.getObjectMapper();

        assertThat(mapper.canSerialize(java.time.LocalDate.class)).isTrue();
        assertThat(mapper.canDeserialize(
                mapper.constructType(java.time.LocalDate.class)
        )).isTrue();
    }
}