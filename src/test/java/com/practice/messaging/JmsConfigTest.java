package com.practice.messaging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JmsConfigTest {

    @Test
    void shouldHaveCorrectTrainingWorkloadQueueName() {
        assertEquals(
                "training-workload",
                JmsConfig.TRAINING_WORKLOAD_QUEUE
        );
    }
}