package com.practice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GymMetricsTest {

    private SimpleMeterRegistry meterRegistry;
    private GymMetricsQueryService queryService;
    private GymMetrics gymMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        queryService = Mockito.mock(GymMetricsQueryService.class);

        gymMetrics = new GymMetrics(
                meterRegistry,
                queryService
        );
    }

    @Test
    void registerGaugesShouldRegisterActiveTraineesGauge() {
        when(queryService.countActiveTrainees())
                .thenReturn(5D);

        gymMetrics.registerGauges();

        Gauge gauge = meterRegistry
                .find("gym_active_trainees")
                .gauge();

        assertThat(gauge).isNotNull();
        assertThat(gauge.value()).isEqualTo(5.0);
    }

    @Test
    void activeTraineesGaugeShouldReturnUpdatedValue() {
        when(queryService.countActiveTrainees())
                .thenReturn(3D)
                .thenReturn(7D);

        gymMetrics.registerGauges();

        Gauge gauge = meterRegistry
                .find("gym_active_trainees")
                .gauge();

        assertThat(gauge).isNotNull();
        assertThat(gauge.value()).isEqualTo(3.0);
        assertThat(gauge.value()).isEqualTo(7.0);
    }

    @Test
    void recordAuthenticationAttemptShouldIncrementSuccessCounter() {
        gymMetrics.recordAuthenticationAttempt(true);
        gymMetrics.recordAuthenticationAttempt(true);

        Counter counter = meterRegistry
                .find("gym_authentication_attempts_total")
                .tag("outcome", "success")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(2.0);
    }

    @Test
    void recordAuthenticationAttemptShouldIncrementFailureCounter() {
        gymMetrics.recordAuthenticationAttempt(false);

        Counter counter = meterRegistry
                .find("gym_authentication_attempts_total")
                .tag("outcome", "failure")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void authenticationCountersShouldBeSeparatedByOutcome() {
        gymMetrics.recordAuthenticationAttempt(true);
        gymMetrics.recordAuthenticationAttempt(false);
        gymMetrics.recordAuthenticationAttempt(false);

        Counter successCounter = meterRegistry
                .find("gym_authentication_attempts_total")
                .tag("outcome", "success")
                .counter();

        Counter failureCounter = meterRegistry
                .find("gym_authentication_attempts_total")
                .tag("outcome", "failure")
                .counter();

        assertThat(successCounter).isNotNull();
        assertThat(failureCounter).isNotNull();

        assertThat(successCounter.count()).isEqualTo(1.0);
        assertThat(failureCounter.count()).isEqualTo(2.0);
    }

    @Test
    void recordRegistrationShouldIncrementTraineeCounter() {
        gymMetrics.recordRegistration("trainee");
        gymMetrics.recordRegistration("trainee");

        Counter counter = meterRegistry
                .find("gym_registrations_total")
                .tag("role", "trainee")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(2.0);
    }

    @Test
    void recordRegistrationShouldIncrementTrainerCounter() {
        gymMetrics.recordRegistration("trainer");

        Counter counter = meterRegistry
                .find("gym_registrations_total")
                .tag("role", "trainer")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void registrationCountersShouldBeSeparatedByRole() {
        gymMetrics.recordRegistration("trainee");
        gymMetrics.recordRegistration("trainer");
        gymMetrics.recordRegistration("trainer");

        Counter traineeCounter = meterRegistry
                .find("gym_registrations_total")
                .tag("role", "trainee")
                .counter();

        Counter trainerCounter = meterRegistry
                .find("gym_registrations_total")
                .tag("role", "trainer")
                .counter();

        assertThat(traineeCounter).isNotNull();
        assertThat(trainerCounter).isNotNull();

        assertThat(traineeCounter.count()).isEqualTo(1.0);
        assertThat(trainerCounter.count()).isEqualTo(2.0);
    }

    @Test
    void stopTrainingCreationTimerShouldRecordExecution() {
        Timer.Sample sample =
                gymMetrics.startTrainingCreationTimer();

        gymMetrics.stopTrainingCreationTimer(sample);

        Timer timer = meterRegistry
                .find("gym_training_creation_seconds")
                .timer();

        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(
                java.util.concurrent.TimeUnit.NANOSECONDS
        )).isGreaterThanOrEqualTo(0);
    }
}