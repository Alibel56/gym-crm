package com.practice.workloadservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.workloadservice.dto.ActionType;
import com.practice.workloadservice.dto.TrainerWorkloadRequest;
import com.practice.workloadservice.logging.TransactionIdFilter;
import com.practice.workloadservice.service.TrainerWorkloadService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TrainingWorkloadListener {

    private static final Logger log =
            LoggerFactory.getLogger(TrainingWorkloadListener.class);

    private final ObjectMapper objectMapper;
    private final TrainerWorkloadService workloadService;

    public TrainingWorkloadListener(
            ObjectMapper objectMapper,
            TrainerWorkloadService workloadService) {

        this.objectMapper = objectMapper;
        this.workloadService = workloadService;
    }

    @JmsListener(
            destination = JmsConfig.TRAINING_WORKLOAD_QUEUE_PROPERTY,
            concurrency = JmsConfig.TRAINING_WORKLOAD_CONCURRENCY_PROPERTY
    )
    public void receive(Message message) throws JMSException {

        String transactionId = extractTransactionId(message);

        try {
            MDC.put(TransactionIdFilter.MDC_KEY, transactionId);

            if (!(message instanceof TextMessage textMessage)) {
                throw new IllegalArgumentException(
                        "Expected TextMessage");
            }

            String json = textMessage.getText();

            log.info("Received workload message: {}", json);

            TrainingWorkloadEvent event;

            try {
                event = objectMapper.readValue(
                        json,
                        TrainingWorkloadEvent.class
                );
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Invalid workload message", e);
            }

            validate(event);

            TrainerWorkloadRequest request =
                    new TrainerWorkloadRequest(
                            event.trainerUsername(),
                            event.trainerFirstName(),
                            event.trainerLastName(),
                            event.isActive(),
                            event.trainingDate(),
                            event.trainingDuration(),
                            ActionType.valueOf(
                                    event.actionType().name()
                            )
                    );

            workloadService.update(request);

            log.info(
                    "Workload updated for trainer={}, action={}",
                    event.trainerUsername(),
                    event.actionType()
            );
        } finally {
            MDC.remove(TransactionIdFilter.MDC_KEY);
        }
    }

    private String extractTransactionId(Message message) throws JMSException {
        String transactionId = message.getStringProperty(JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY);
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
            log.debug("op=extractTransactionId - no transactionId on message, generated={}", transactionId);
        }
        return transactionId;
    }

    private void validate(TrainingWorkloadEvent event) {

        if (event.trainerUsername() == null ||
                event.trainerUsername().isBlank()) {
            throw new IllegalArgumentException(
                    "trainerUsername is required");
        }

        if (event.trainerFirstName() == null ||
                event.trainerFirstName().isBlank()) {
            throw new IllegalArgumentException(
                    "trainerFirstName is required");
        }

        if (event.trainerLastName() == null ||
                event.trainerLastName().isBlank()) {
            throw new IllegalArgumentException(
                    "trainerLastName is required");
        }

        if (event.isActive() == null) {
            throw new IllegalArgumentException(
                    "isActive is required");
        }

        if (event.trainingDate() == null) {
            throw new IllegalArgumentException(
                    "trainingDate is required");
        }

        if (event.trainingDuration() == null ||
                event.trainingDuration() <= 0) {
            throw new IllegalArgumentException(
                    "trainingDuration must be positive");
        }

        if (event.actionType() == null) {
            throw new IllegalArgumentException(
                    "actionType is required");
        }
    }
}
