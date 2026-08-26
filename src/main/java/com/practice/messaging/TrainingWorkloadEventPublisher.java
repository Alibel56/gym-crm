package com.practice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.logging.TransactionIdFilter;
import org.slf4j.MDC;
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
            String transactionId = MDC.get(TransactionIdFilter.MDC_KEY);

            jmsTemplate.convertAndSend(
                    trainingWorkloadQueue,
                    message,
                    jmsMessage -> {
                        if (transactionId != null && !transactionId.isBlank()) {
                            jmsMessage.setStringProperty(
                                    JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY, transactionId);
                        }
                        return jmsMessage;
                    }
            );

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize training workload event", e);
        }
    }
}
