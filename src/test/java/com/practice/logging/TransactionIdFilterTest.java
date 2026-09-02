package com.practice.logging;

import com.practice.testtags.EdgeCaseTest;
import com.practice.testtags.ExceptionTest;
import com.practice.testtags.NegativeTest;
import com.practice.testtags.PositiveTest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TransactionIdFilterTest {

    private TransactionIdFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TransactionIdFilter();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @PositiveTest
    void doFilter_shouldUseExistingTransactionId()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        request.addHeader(TransactionIdFilter.HEADER_NAME, "tx-123");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);

        assertThat(response.getHeader(TransactionIdFilter.HEADER_NAME))
                .isEqualTo("tx-123");

        assertThat(MDC.get(TransactionIdFilter.MDC_KEY))
                .isNull();
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void doFilter_shouldGenerateTransactionIdWhenHeaderMissing()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);

        String transactionId =
                response.getHeader(TransactionIdFilter.HEADER_NAME);

        assertThat(transactionId).isNotBlank();

        assertThat(UUID.fromString(transactionId))
                .isNotNull();

        assertThat(MDC.get(TransactionIdFilter.MDC_KEY))
                .isNull();
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void doFilter_shouldClearMdcEvenWhenExceptionOccurs()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        doThrow(new ServletException("boom"))
                .when(chain)
                .doFilter(request, response);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        filter.doFilter(request, response, chain))
                .isInstanceOf(ServletException.class);

        assertThat(MDC.get(TransactionIdFilter.MDC_KEY))
                .isNull();
    }
}
