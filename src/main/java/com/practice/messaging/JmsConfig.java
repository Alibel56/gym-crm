package com.practice.messaging;

public final class JmsConfig {

    private JmsConfig() {
    }

    public static final String TRAINING_WORKLOAD_QUEUE_PROPERTY =
            "${jms.queue.training-workload}";
}
