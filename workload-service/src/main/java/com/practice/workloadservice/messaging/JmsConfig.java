package com.practice.workloadservice.messaging;

public final class JmsConfig {

    private JmsConfig() {
    }

    public static final String TRAINING_WORKLOAD_QUEUE_PROPERTY =
            "${jms.queue.training-workload}";

    public static final String TRAINING_WORKLOAD_CONCURRENCY_PROPERTY =
            "${jms.listener.concurrency:3-10}";
}
