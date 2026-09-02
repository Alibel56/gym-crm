package com.practice.config;

import com.practice.logging.TransactionIdFilter;
import com.practice.testtags.PositiveTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

class FilterConfigTest {

    private final FilterConfig filterConfig = new FilterConfig();

    @Test
    @PositiveTest
    void transactionIdFilter_registersTransactionIdFilterInstance() {
        FilterRegistrationBean<TransactionIdFilter> registration = filterConfig.transactionIdFilter();

        assertThat(registration.getFilter()).isInstanceOf(TransactionIdFilter.class);
    }

    @Test
    @PositiveTest
    void transactionIdFilter_isMappedToAllUrls() {
        FilterRegistrationBean<TransactionIdFilter> registration = filterConfig.transactionIdFilter();

        assertThat(registration.getUrlPatterns()).containsExactly("/*");
    }

    @Test
    @PositiveTest
    void transactionIdFilter_hasNameAndOrderConfigured() {
        FilterRegistrationBean<TransactionIdFilter> registration = filterConfig.transactionIdFilter();

        assertThat(registration.getFilterName()).isEqualTo("transactionIdFilter");
        assertThat(registration.getOrder()).isEqualTo(1);
    }
}
