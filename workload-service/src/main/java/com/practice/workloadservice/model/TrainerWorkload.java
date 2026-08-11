package com.practice.workloadservice.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrainerWorkload {
    private final String username;
    private volatile String firstName;
    private volatile String lastName;
    private volatile boolean active;
    private final Map<Integer, Map<Integer, Integer>> years = new ConcurrentHashMap<>();

    public TrainerWorkload(String username, String firstName, String lastName, boolean active) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }

    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public boolean isActive() { return active; }
    public Map<Integer, Map<Integer, Integer>> getYears() { return years; }

    public void updateIdentity(String firstName, String lastName, boolean active) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }
}
