package com.practice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void publish_shouldSerializeAndSendMessage() throws Exception {
        String json = "{\"trainingId\":1,\"action\":\"CREATE\"}";

        when(objectMapper.writeValueAsString(event))
                .thenReturn(json);

        publisher.publish(event);

        verify(objectMapper).writeValueAsString(event);
        verify(jmsTemplate).convertAndSend(
                QUEUE_NAME,
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