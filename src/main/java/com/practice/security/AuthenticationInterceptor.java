package com.practice.security;

import com.practice.dto.request.LoginRequest;
import com.practice.exception.AuthenticationException;
import com.practice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BASIC_PREFIX = "Basic ";

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        LoginRequest credentials = extractCredentials(request);

        userService.authenticate(credentials);

        return true;
    }

    private LoginRequest extractCredentials(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);

        if (header == null || !header.startsWith(BASIC_PREFIX)) {
            throw new AuthenticationException(
                    "Missing or malformed Authorization header"
            );
        }

        String encodedCredentials = header.substring(BASIC_PREFIX.length()).trim();

        if (encodedCredentials.isBlank()) {
            throw new AuthenticationException(
                    "Missing or malformed Authorization header"
            );
        }

        String decoded;

        try {
            byte[] rawCredentials = Base64.getDecoder()
                    .decode(encodedCredentials);

            decoded = new String(
                    rawCredentials,
                    StandardCharsets.UTF_8
            );
        } catch (IllegalArgumentException exception) {
            throw new AuthenticationException(
                    "Malformed Authorization header"
            );
        }

        int separatorIndex = decoded.indexOf(':');

        if (separatorIndex <= 0) {
            throw new AuthenticationException(
                    "Malformed Authorization header"
            );
        }

        String userName = decoded.substring(0, separatorIndex);
        String password = decoded.substring(separatorIndex + 1);

        if (userName.isBlank() || password.isBlank()) {
            throw new AuthenticationException(
                    "Username and password must not be blank"
            );
        }

        return new LoginRequest(userName, password);
    }
}