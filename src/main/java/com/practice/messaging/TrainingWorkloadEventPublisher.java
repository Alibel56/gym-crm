package com.practice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class TrainingWorkloadEventPublisher {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    public TrainingWorkloadEventPublisher(
            JmsTemplate jmsTemplate,
            ObjectMapper objectMapper) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(TrainingWorkloadEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);

            jmsTemplate.convertAndSend(
                    JmsConfig.TRAINING_WORKLOAD_QUEUE,
                    message
            );

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize training workload event", e);
        }
    }
}