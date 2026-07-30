package com.practice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WebConfigTest {

    @Test
    void extendMessageConvertersShouldAddJacksonConverterWithJavaTimeModule() {
        WebConfig webConfig = new WebConfig();

        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        webConfig.extendMessageConverters(converters);

        assertThat(converters).hasSize(1);
        assertThat(converters.getFirst())
                .isInstanceOf(MappingJackson2HttpMessageConverter.class);

        MappingJackson2HttpMessageConverter converter =
                (MappingJackson2HttpMessageConverter) converters.getFirst();

        ObjectMapper objectMapper = converter.getObjectMapper();

        assertThat(objectMapper.getRegisteredModuleIds())
                .contains(new JavaTimeModule().getTypeId());
    }

    @Test
    void setAuthenticationInterceptorShouldNotThrowException() {
        WebConfig webConfig = new WebConfig();

        webConfig.setAuthenticationInterceptor(null);
    }
}