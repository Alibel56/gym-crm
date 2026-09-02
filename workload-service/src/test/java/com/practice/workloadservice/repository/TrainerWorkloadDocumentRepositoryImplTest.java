package com.practice.workloadservice.repository;

import com.practice.testtags.PositiveTest;
import com.practice.testtags.NegativeTest;
import com.practice.testtags.EdgeCaseTest;
import com.practice.testtags.ExceptionTest;

import com.practice.workloadservice.model.TrainerWorkloadDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadDocumentRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private TrainerWorkloadDocument document;

    private TrainerWorkloadDocumentRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new TrainerWorkloadDocumentRepositoryImpl(mongoTemplate);
    }

    @Test
    @PositiveTest
    void findByUsername_shouldReturnDocument_whenDocumentExists() {
        // Arrange
        String username = "john.doe";

        when(mongoTemplate.findById(
                username,
                TrainerWorkloadDocument.class
        )).thenReturn(document);

        // Act
        Optional<TrainerWorkloadDocument> result =
                repository.findByUsername(username);

        // Assert
        assertTrue(result.isPresent());
        assertSame(document, result.get());

        verify(mongoTemplate).findById(
                username,
                TrainerWorkloadDocument.class
        );
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    void findByUsername_shouldReturnEmpty_whenDocumentDoesNotExist() {
        // Arrange
        String username = "unknown";

        when(mongoTemplate.findById(
                username,
                TrainerWorkloadDocument.class
        )).thenReturn(null);

        // Act
        Optional<TrainerWorkloadDocument> result =
                repository.findByUsername(username);

        // Assert
        assertTrue(result.isEmpty());

        verify(mongoTemplate).findById(
                username,
                TrainerWorkloadDocument.class
        );
    }

    @Test
    @PositiveTest
    void save_shouldReturnSavedDocument() {
        // Arrange
        when(document.getTrainerUsername()).thenReturn("john.doe");
        when(mongoTemplate.save(document)).thenReturn(document);

        // Act
        TrainerWorkloadDocument result =
                repository.save(document);

        // Assert
        assertSame(document, result);

        verify(document, times(2)).getTrainerUsername();
        verify(mongoTemplate).save(document);
    }
}
