package com.practice.workloadservice.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
@Component
public class RestCallLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger("com.practice.workload.rest.CallLog");
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Around("execution(public * com.practice.workloadservice.controller..*(..))")
    public Object logRestCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs
                ? attrs.getRequest().getMethod() : "";
        String endpoint = method + " " + joinPoint.getSignature().toShortString();
        log.info("REST call started endpoint={} request={}", endpoint, toJson(joinPoint.getArgs()));
        try {
            Object result = joinPoint.proceed();
            log.info("REST call finished endpoint={} response=200 OK body={}", endpoint, toJson(result));
            return result;
        } catch (Exception ex) {
            log.warn("REST call failed endpoint={} error={} message={}", endpoint,
                    ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

    private String toJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception ex) { return String.valueOf(value); }
    }
}
