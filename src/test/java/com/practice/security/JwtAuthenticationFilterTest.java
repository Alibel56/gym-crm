package com.practice.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private TokenBlacklistService blacklistService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(
                jwtService,
                userDetailsService,
                blacklistService
        );

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueFilterChainWhenAuthorizationHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService, blacklistService);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValid() throws Exception {
        String token = "valid-token";
        String tokenId = "token-id";
        String username = "john";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername(username)
                        .password("encoded-password")
                        .authorities(List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ))
                        .build();

        when(jwtService.extractTokenId(token)).thenReturn(tokenId);
        when(blacklistService.isRevoked(tokenId)).thenReturn(false);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username))
                .thenReturn(userDetails);
        when(jwtService.isValid(token, userDetails)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNotNull();

        assertThat(SecurityContextHolder.getContext()
                .getAuthentication()
                .getName())
                .isEqualTo(username);

        assertThat(SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateUserWhenTokenIsRevoked() throws Exception {
        String token = "revoked-token";
        String tokenId = "token-id";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractTokenId(token)).thenReturn(tokenId);
        when(blacklistService.isRevoked(tokenId)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractTokenId(token);
        verify(blacklistService).isRevoked(tokenId);

        verify(jwtService, never()).extractUsername(anyString());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void shouldClearSecurityContextWhenTokenIsInvalid() throws Exception {
        String token = "invalid-token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractTokenId(token))
                .thenThrow(new JwtException("Invalid token"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();

        verify(filterChain).doFilter(request, response);
    }
}