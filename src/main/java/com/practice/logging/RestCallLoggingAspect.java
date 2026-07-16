package com.practice.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Set;

@Aspect
@Component
public class RestCallLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger("com.practice.rest.CallLog");
    private static final Set<Class<?>> NON_LOGGABLE_TYPES = Set.of(
            HttpServletRequest.class, HttpServletResponse.class);

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Around("execution(public * com.practice.controller..*(..))")
    public Object logRestCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String endpoint = describeEndpoint(joinPoint);
        String requestPayload = describeArgs(joinPoint.getArgs());

        log.info("REST call started endpoint={} request={}", endpoint, requestPayload);

        try {
            Object result = joinPoint.proceed();
            log.info("REST call finished endpoint={} response=200 OK body={}", endpoint, toJson(result));
            return result;
        } catch (Exception ex) {
            log.warn("REST call failed endpoint={} error={} message={}",
                    endpoint, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

    private String describeEndpoint(ProceedingJoinPoint joinPoint) {
        String httpMethod = "";
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            httpMethod = attrs.getRequest().getMethod();
        }
        return httpMethod + " " + joinPoint.getSignature().toShortString();
    }

    private String describeArgs(Object[] args) {
        Object[] loggable = Arrays.stream(args)
                .filter(arg -> arg == null || NON_LOGGABLE_TYPES.stream().noneMatch(t -> t.isInstance(arg)))
                .toArray();
        return toJson(loggable);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }
}
