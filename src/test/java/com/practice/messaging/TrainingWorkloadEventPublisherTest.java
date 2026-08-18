package com.practice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingWorkloadEventPublisherTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TrainingWorkloadEvent event;

    @InjectMocks
    private TrainingWorkloadEventPublisher publisher;

    @Test
    void publish_shouldSerializeAndSendMessage() throws Exception {
        String json = "{\"trainingId\":1,\"action\":\"CREATE\"}";

        when(objectMapper.writeValueAsString(event))
                .thenReturn(json);

        publisher.publish(event);

        verify(objectMapper).writeValueAsString(event);
        verify(jmsTemplate).convertAndSend(
                JmsConfig.TRAINING_WORKLOAD_QUEUE,
                json
        );
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