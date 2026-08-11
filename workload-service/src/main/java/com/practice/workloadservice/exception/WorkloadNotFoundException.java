package com.practice.workloadservice.exception;

public class WorkloadNotFoundException extends RuntimeException {
    public WorkloadNotFoundException(String message) {
        super(message);
    }
}
