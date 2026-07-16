package com.practice.config;

import com.practice.logging.TransactionIdFilter;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebAppInitializerTest {

    private final WebAppInitializer initializer = new WebAppInitializer();

    @Test
    void shouldReturnRootConfigClasses() {
        Class<?>[] classes = initializer.getRootConfigClasses();

        assertThat(classes)
                .containsExactly(
                        AppConfig.class,
                        DataSourceConfig.class,
                        HibernateConfig.class
                );
    }

    @Test
    void shouldReturnServletConfigClasses() {
        Class<?>[] classes = initializer.getServletConfigClasses();

        assertThat(classes)
                .containsExactly(WebConfig.class);
    }

    @Test
    void shouldReturnServletMappings() {
        String[] mappings = initializer.getServletMappings();

        assertThat(mappings)
                .containsExactly("/");
    }

    @Test
    void shouldReturnTransactionIdFilter() {
        Filter[] filters = initializer.getServletFilters();

        assertThat(filters)
                .hasSize(1);

        assertThat(filters[0])
                .isInstanceOf(TransactionIdFilter.class);
    }
}