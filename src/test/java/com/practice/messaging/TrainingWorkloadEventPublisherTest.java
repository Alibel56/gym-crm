package com.practice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.logging.TransactionIdFilter;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingWorkloadEventPublisherTest {

    private static final String QUEUE_NAME = "training-workload-test";

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TrainingWorkloadEvent event;

    private TrainingWorkloadEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new TrainingWorkloadEventPublisher(
                jmsTemplate, objectMapper, QUEUE_NAME);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void publish_shouldSerializeAndSendMessage() throws Exception {
        String json = "{\"trainingId\":1,\"action\":\"CREATE\"}";

        when(objectMapper.writeValueAsString(event))
                .thenReturn(json);

        publisher.publish(event);

        verify(objectMapper).writeValueAsString(event);
        verify(jmsTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq(QUEUE_NAME),
                org.mockito.ArgumentMatchers.eq(json),
                any(MessagePostProcessor.class)
        );
    }

    @Test
    void publish_shouldAttachTransactionIdFromMdc_whenPresent() throws Exception {
        String json = "{\"trainingId\":1,\"action\":\"CREATE\"}";
        String transactionId = "tx-123";
        MDC.put(TransactionIdFilter.MDC_KEY, transactionId);

        when(objectMapper.writeValueAsString(event)).thenReturn(json);

        publisher.publish(event);

        ArgumentCaptor<MessagePostProcessor> captor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq(QUEUE_NAME),
                org.mockito.ArgumentMatchers.eq(json),
                captor.capture());

        Message jmsMessage = mock(Message.class);
        captor.getValue().postProcessMessage(jmsMessage);
        verify(jmsMessage).setStringProperty(JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY, transactionId);
    }

    @Test
    void publish_shouldNotAttachTransactionId_whenAbsentFromMdc() throws Exception {
        String json = "{\"trainingId\":1,\"action\":\"CREATE\"}";

        when(objectMapper.writeValueAsString(event)).thenReturn(json);

        publisher.publish(event);

        ArgumentCaptor<MessagePostProcessor> captor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq(QUEUE_NAME),
                org.mockito.ArgumentMatchers.eq(json),
                captor.capture());

        Message jmsMessage = mock(Message.class);
        captor.getValue().postProcessMessage(jmsMessage);
        verify(jmsMessage, org.mockito.Mockito.never())
                .setStringProperty(org.mockito.ArgumentMatchers.eq(JmsConfig.TRANSACTION_ID_MESSAGE_PROPERTY),
                        any());
    }

    @Test
    void publish_shouldThrowIllegalStateException_whenSerializationFails()
            throws Exception {

        JsonProcessingException exception =
                new JsonProcessingException("Serialization failed") {
                };

        when(objectMapper.writeValueAsString(event))
                .thenThrow(exception);

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> publisher.publish(event)
        );

        assertEquals(
                "Failed to serialize training workload event",
                thrown.getMessage()
        );

        verify(objectMapper).writeValueAsString(event);
    }
}
