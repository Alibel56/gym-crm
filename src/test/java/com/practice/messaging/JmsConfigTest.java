package com.practice.messaging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JmsConfigTest {

    @Test
    void shouldExposePlaceholderForTrainingWorkloadQueueProperty() {
        assertEquals(
                "${jms.queue.training-workload}",
                JmsConfig.TRAINING_WORKLOAD_QUEUE_PROPERTY
        );
    }
}
