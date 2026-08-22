package com.practice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class TrainingWorkloadEventPublisher {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final String trainingWorkloadQueue;

    public TrainingWorkloadEventPublisher(
            JmsTemplate jmsTemplate,
            ObjectMapper objectMapper,
            @Value(JmsConfig.TRAINING_WORKLOAD_QUEUE_PROPERTY) String trainingWorkloadQueue) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
        this.trainingWorkloadQueue = trainingWorkloadQueue;
    }

    public void publish(TrainingWorkloadEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);

            jmsTemplate.convertAndSend(
                    trainingWorkloadQueue,
                    message
            );

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize training workload event", e);
        }
    }
}