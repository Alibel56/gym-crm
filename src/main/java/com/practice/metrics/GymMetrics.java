package com.practice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {

    private static final Logger log =
            LoggerFactory.getLogger(GymMetrics.class);

    private final MeterRegistry meterRegistry;
    private final GymMetricsQueryService queryService;

    public GymMetrics(
            MeterRegistry meterRegistry,
            GymMetricsQueryService queryService
    ) {
        this.meterRegistry = meterRegistry;
        this.queryService = queryService;
    }

    @PostConstruct
    public void registerGauges() {
        Gauge.builder(
                        "gym_active_trainees",
                        queryService,
                        GymMetricsQueryService::countActiveTrainees
                )
                .description(
                        "Current number of trainees with an active profile"
                )
                .register(meterRegistry);

        log.debug("Registered gym_active_trainees gauge");
    }

    public void recordAuthenticationAttempt(boolean success) {
        Counter.builder("gym_authentication_attempts_total")
                .description(
                        "Authentication attempts against the Basic Auth interceptor"
                )
                .tags(Tags.of(
                        "outcome",
                        success ? "success" : "failure"
                ))
                .register(meterRegistry)
                .increment();
    }

    public void recordRegistration(String role) {
        Counter.builder("gym_registrations_total")
                .description(
                        "New trainee/trainer profile registrations"
                )
                .tags(Tags.of("role", role))
                .register(meterRegistry)
                .increment();
    }

    public Timer.Sample startTrainingCreationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTrainingCreationTimer(Timer.Sample sample) {
        sample.stop(
                Timer.builder("gym_training_creation_seconds")
                        .description(
                                "Time taken to persist a new training"
                        )
                        .register(meterRegistry)
        );
    }
}