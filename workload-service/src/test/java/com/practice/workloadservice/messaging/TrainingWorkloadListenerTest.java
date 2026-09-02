package com.practice.workloadservice.messaging;

import com.practice.testtags.PositiveTest;
import com.practice.testtags.NegativeTest;
import com.practice.testtags.EdgeCaseTest;
import com.practice.testtags.ExceptionTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.workloadservice.dto.ActionType;
import com.practice.workloadservice.dto.TrainerWorkloadRequest;
import com.practice.workloadservice.service.TrainerWorkloadService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingWorkloadListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TrainerWorkloadService workloadService;

    @Mock
    private TextMessage textMessage;

    @Mock
    private Message message;

    private TrainingWorkloadListener listener;

    @BeforeEach
    void setUp() {
        listener = new TrainingWorkloadListener(
                objectMapper,
                workloadService
        );

        MDC.clear();
    }

    @Test
    @PositiveTest
    void receive_shouldProcessValidMessage() throws Exception {
        String transactionId = UUID.randomUUID().toString();

        TrainingWorkloadEvent event = validEvent();

        when(textMessage.getStringProperty(
                JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY))
                .thenReturn(transactionId);

        when(textMessage.getText())
                .thenReturn("{}");

        when(objectMapper.readValue(
                "{}",
                TrainingWorkloadEvent.class))
                .thenReturn(event);

        listener.receive(textMessage);

        ArgumentCaptor<TrainerWorkloadRequest> captor =
                ArgumentCaptor.forClass(TrainerWorkloadRequest.class);

        verify(workloadService).update(captor.capture());

        TrainerWorkloadRequest request = captor.getValue();

        assertEquals("john.doe", request.trainerUsername());
        assertEquals("John", request.trainerFirstName());
        assertEquals("Doe", request.trainerLastName());
        assertTrue(request.isActive());
        assertEquals(LocalDate.of(2026, 8, 26),
                request.trainingDate());
        assertEquals(90, request.trainingDuration());
        assertEquals(ActionType.ADD, request.actionType());

        assertNull(MDC.get("transactionId"));
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void receive_shouldGenerateTransactionId_whenPropertyMissing()
            throws Exception {

        TrainingWorkloadEvent event = validEvent();

        when(textMessage.getStringProperty(
                JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY))
                .thenReturn(null);

        when(textMessage.getText())
                .thenReturn("{}");

        when(objectMapper.readValue(
                "{}",
                TrainingWorkloadEvent.class))
                .thenReturn(event);

        listener.receive(textMessage);

        verify(workloadService).update(any(TrainerWorkloadRequest.class));

        assertNull(MDC.get("transactionId"));
    }

    @Test
    @EdgeCaseTest
    void receive_shouldGenerateTransactionId_whenPropertyBlank()
            throws Exception {

        TrainingWorkloadEvent event = validEvent();

        when(textMessage.getStringProperty(
                JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY))
                .thenReturn("   ");

        when(textMessage.getText())
                .thenReturn("{}");

        when(objectMapper.readValue(
                "{}",
                TrainingWorkloadEvent.class))
                .thenReturn(event);

        listener.receive(textMessage);

        verify(workloadService).update(any(TrainerWorkloadRequest.class));

        assertNull(MDC.get("transactionId"));
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void receive_shouldThrowException_whenMessageIsNotTextMessage()
            throws Exception {

        when(message.getStringProperty(
                JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY))
                .thenReturn("transaction-123");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> listener.receive(message)
                );

        assertEquals(
                "Expected TextMessage",
                exception.getMessage()
        );

        verifyNoInteractions(workloadService);
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void receive_shouldThrowException_whenJsonIsInvalid()
            throws Exception {

        when(textMessage.getStringProperty(
                JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY))
                .thenReturn("transaction-123");

        when(textMessage.getText())
                .thenReturn("invalid-json");

        when(objectMapper.readValue(
                "invalid-json",
                TrainingWorkloadEvent.class))
                .thenThrow(new RuntimeException("JSON error"));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> listener.receive(textMessage)
                );

        assertEquals(
                "Invalid workload message",
                exception.getMessage()
        );

        verifyNoInteractions(workloadService);
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void receive_shouldRejectNullTrainerUsername()
            throws Exception {

        assertValidationError(
                new TrainingWorkloadEvent(
                        null,
                        "John",
                        "Doe",
                        true,
                        LocalDate.of(2026, 8, 26),
                        90,
                        WorkloadActionType.ADD
                ),
                "trainerUsername is required"
        );
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void receive_shouldRejectBlankTrainerUsername()
            throws Exception {

        assertValidationError(
                new TrainingWorkloadEvent(
                        "   ",
                        "John",
                        "Doe",
                        true,
                        LocalDate.of(2026, 8, 26),
                        90,
                        WorkloadActionType.ADD
                ),
                "trainerUsername is required"
        );
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void receive_shouldRejectNullFirstName()
            throws Exception {

        assertValidationError(
                new TrainingWorkloadEvent(
                        "john.doe",
                        null,
                        "Doe",
                        true,
                        LocalDate.of(2026, 8, 26),
                        90,
                        WorkloadActionType.ADD
                ),
                "trainerFirstName is required"
        );
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void receive_shouldRejectBlankFirstName()
            throws Exception {

        assertValidationError(
                new TrainingWorkloadEvent(
                        "john.doe",
                        "   ",
                        "Doe",
                        true,
                        LocalDate.of(2026, 8, 26),
                        90,
                        WorkloadActionType.ADD
                ),
                "trainerFirstName is required"
        );
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void receive_shouldRejectNullLastName()
            throws Exception {

        assertValidationError(
                new TrainingWorkloadEvent(
                        "john.doe",
                        "John",
                        null,
                        true,
                        LocalDate.of(2026, 8, 26),
                        90,
                        WorkloadActionType.ADD
                ),
                "trainerLastName is required"
        );
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void receive_shouldRejectBlankLastName()
            throws Exception {

        assertValidationError(
                new TrainingWorkloadEvent(
                        "john.doe",
                        "John",
                        "   ",
                        true,
                        LocalDate.of(2026, 8, 26),
                        90,
                        WorkloadActionType.ADD
                ),
                "trainerLastName is required"
        );
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void receive_shouldRejectNullIsActive()
            throws Exception {

        assertValidationError(
                new TrainingWorkloadEvent(
                        "john.doe",
                        "John",
                        "Doe",
                        null,
                        LocalDate.of(2026, 8, 26),
                        90,
                        WorkloadActionType.ADD
                ),
                "isActive is required"
        );
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void receive_shouldRejectNullTrainingDate()
            throws Exception {

        assertValidationError(
                new TrainingWorkloadEvent(
                        "john.doe",
                        "John",
                        "Doe",
                        true,
                        null,
                        90,
                        WorkloadActionType.ADD
                ),
                "trainingDate is required"
        );
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void receive_shouldRejectNullTrainingDuration()
            throws Exception {

        assertValidationError(
                new TrainingWorkloadEvent(
                        "john.doe",
                        "John",
                        "Doe",
                        true,
                        LocalDate.of(2026, 8, 26),
                        null,
                        WorkloadActionType.ADD
                ),
                "trainingDuration must be positive"
        );
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void receive_shouldRejectZeroTrainingDuration()
            throws Exception {

        assertValidationError(
                new TrainingWorkloadEvent(
                        "john.doe",
                        "John",
                        "Doe",
                        true,
                        LocalDate.of(2026, 8, 26),
                        0,
                        WorkloadActionType.ADD
                ),
                "trainingDuration must be positive"
        );
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void receive_shouldRejectNegativeTrainingDuration()
            throws Exception {

        assertValidationError(
                new TrainingWorkloadEvent(
                        "john.doe",
                        "John",
                        "Doe",
                        true,
                        LocalDate.of(2026, 8, 26),
                        -10,
                        WorkloadActionType.ADD
                ),
                "trainingDuration must be positive"
        );
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void receive_shouldRejectNullActionType()
            throws Exception {

        assertValidationError(
                new TrainingWorkloadEvent(
                        "john.doe",
                        "John",
                        "Doe",
                        true,
                        LocalDate.of(2026, 8, 26),
                        90,
                        null
                ),
                "actionType is required"
        );
    }

    @Test
    @NegativeTest
    void receive_shouldNotCallService_whenValidationFails()
            throws Exception {

        TrainingWorkloadEvent event =
                new TrainingWorkloadEvent(
                        null,
                        "John",
                        "Doe",
                        true,
                        LocalDate.of(2026, 8, 26),
                        90,
                        WorkloadActionType.ADD
                );

        when(textMessage.getStringProperty(
                JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY))
                .thenReturn("transaction-123");

        when(textMessage.getText())
                .thenReturn("{}");

        when(objectMapper.readValue(
                "{}",
                TrainingWorkloadEvent.class))
                .thenReturn(event);

        assertThrows(
                IllegalArgumentException.class,
                () -> listener.receive(textMessage)
        );

        verifyNoInteractions(workloadService);
    }

    @Test
    @PositiveTest
    void receive_shouldRemoveMdcAfterSuccessfulProcessing()
            throws Exception {

        when(textMessage.getStringProperty(
                JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY))
                .thenReturn("transaction-123");

        when(textMessage.getText())
                .thenReturn("{}");

        when(objectMapper.readValue(
                "{}",
                TrainingWorkloadEvent.class))
                .thenReturn(validEvent());

        listener.receive(textMessage);

        assertNull(MDC.get("transactionId"));
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void receive_shouldRemoveMdcWhenExceptionOccurs()
            throws Exception {

        when(textMessage.getStringProperty(
                JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY))
                .thenReturn("transaction-123");

        when(textMessage.getText())
                .thenReturn("{}");

        when(objectMapper.readValue(
                "{}",
                TrainingWorkloadEvent.class))
                .thenThrow(new RuntimeException("JSON error"));

        assertThrows(
                IllegalArgumentException.class,
                () -> listener.receive(textMessage)
        );

        assertNull(MDC.get("transactionId"));
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void receive_shouldPropagateJmsExceptionFromGetText()
            throws Exception {

        when(textMessage.getStringProperty(
                JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY))
                .thenReturn("transaction-123");

        when(textMessage.getText())
                .thenThrow(new JMSException("JMS error"));

        assertThrows(
                JMSException.class,
                () -> listener.receive(textMessage)
        );

        verifyNoInteractions(objectMapper);
        verifyNoInteractions(workloadService);
    }

    private TrainingWorkloadEvent validEvent() {
        return new TrainingWorkloadEvent(
                "john.doe",
                "John",
                "Doe",
                true,
                LocalDate.of(2026, 8, 26),
                90,
                WorkloadActionType.ADD
        );
    }

    private void assertValidationError(
            TrainingWorkloadEvent event,
            String expectedMessage) throws Exception {

        when(textMessage.getStringProperty(
                JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY))
                .thenReturn("transaction-123");

        when(textMessage.getText())
                .thenReturn("{}");

        when(objectMapper.readValue(
                "{}",
                TrainingWorkloadEvent.class))
                .thenReturn(event);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> listener.receive(textMessage)
                );

        assertEquals(
                expectedMessage,
                exception.getMessage()
        );

        verifyNoInteractions(workloadService);

        assertNull(MDC.get("transactionId"));
    }
}
