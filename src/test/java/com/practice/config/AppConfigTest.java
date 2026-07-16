package com.practice.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

class AppConfigTest {

    @Test
    void propertySourcesPlaceholderConfigurer_shouldCreateBean() {

        PropertySourcesPlaceholderConfigurer bean =
                AppConfig.propertySourcesPlaceholderConfigurer();

        assertThat(bean).isNotNull();
    }
}