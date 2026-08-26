package com.practice.workloadservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.workloadservice.dto.TrainerWorkloadRequest;
import com.practice.workloadservice.logging.TransactionIdFilter;
import com.practice.workloadservice.service.TrainerWorkloadService;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingWorkloadListenerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private TrainerWorkloadService workloadService;

    @Mock
    private TextMessage textMessage;

    private TrainingWorkloadListener listener;

    private String validJson() {
        return """
                {
                  "trainerUsername": "john.doe",
                  "trainerFirstName": "John",
                  "trainerLastName": "Doe",
                  "isActive": true,
                  "trainingDate": "2026-03-15",
                  "trainingDuration": 60,
                  "actionType": "ADD"
                }
                """;
    }

    @Test
    void receive_shouldPutTransactionIdOnMdc_duringProcessing_andClearItAfterward() throws Exception {
        listener = new TrainingWorkloadListener(objectMapper, workloadService);

        when(textMessage.getStringProperty(JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY)).thenReturn("tx-42");
        when(textMessage.getText()).thenReturn(validJson());

        String[] observedTxId = new String[1];
        doAnswer(invocation -> {
            observedTxId[0] = MDC.get(TransactionIdFilter.MDC_KEY);
            return null;
        }).when(workloadService).update(org.mockito.ArgumentMatchers.any(TrainerWorkloadRequest.class));

        listener.receive(textMessage);

        assertEquals("tx-42", observedTxId[0]);
        assertNull(MDC.get(TransactionIdFilter.MDC_KEY));
    }

    @Test
    void receive_shouldGenerateTransactionId_whenMessageHasNone() throws Exception {
        listener = new TrainingWorkloadListener(objectMapper, workloadService);

        when(textMessage.getStringProperty(JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY)).thenReturn(null);
        when(textMessage.getText()).thenReturn(validJson());

        String[] observedTxId = new String[1];
        doAnswer(invocation -> {
            observedTxId[0] = MDC.get(TransactionIdFilter.MDC_KEY);
            return null;
        }).when(workloadService).update(org.mockito.ArgumentMatchers.any(TrainerWorkloadRequest.class));

        listener.receive(textMessage);

        assertEquals(true, observedTxId[0] != null && !observedTxId[0].isBlank());
        assertNull(MDC.get(TransactionIdFilter.MDC_KEY));
    }

    @Test
    void receive_shouldMapEventFieldsIntoRequest() throws Exception {
        listener = new TrainingWorkloadListener(objectMapper, workloadService);

        when(textMessage.getStringProperty(JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY)).thenReturn("tx-1");
        when(textMessage.getText()).thenReturn(validJson());

        listener.receive(textMessage);

        ArgumentCaptor<TrainerWorkloadRequest> captor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(workloadService).update(captor.capture());
        TrainerWorkloadRequest request = captor.getValue();

        assertEquals("john.doe", request.trainerUsername());
        assertEquals("John", request.trainerFirstName());
        assertEquals("Doe", request.trainerLastName());
        assertEquals(LocalDate.of(2026, 3, 15), request.trainingDate());
        assertEquals(60, request.trainingDuration());
    }

    @Test
    void receive_shouldClearMdc_evenWhenProcessingFails() throws Exception {
        listener = new TrainingWorkloadListener(objectMapper, workloadService);

        when(textMessage.getStringProperty(JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY)).thenReturn("tx-err");
        when(textMessage.getText()).thenReturn("not-json");

        assertThrows(IllegalArgumentException.class, () -> listener.receive(textMessage));
        assertNull(MDC.get(TransactionIdFilter.MDC_KEY));
    }
}
