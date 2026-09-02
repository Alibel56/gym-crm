package com.practice.config;

import com.practice.dao.TrainingTypeRepository;
import com.practice.entity.TrainingType;
import com.practice.testtags.EdgeCaseTest;
import com.practice.testtags.PositiveTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer = new DataInitializer();
        dataInitializer.setTrainingTypeRepository(trainingTypeRepository);
    }

    @Test
    @PositiveTest
    void init_shouldNotSaveTrainingTypes_whenTrainingTypesAlreadyExist() {
        TrainingType existingTrainingType = TrainingType.builder()
                .name("Fitness")
                .build();

        when(trainingTypeRepository.findAll())
                .thenReturn(List.of(existingTrainingType));

        dataInitializer.init();

        verify(trainingTypeRepository).findAll();
        verify(trainingTypeRepository, never()).save(any(TrainingType.class));
        verifyNoMoreInteractions(trainingTypeRepository);
    }

    @Test
    @EdgeCaseTest
    void init_shouldSaveDefaultTrainingTypes_whenRepositoryIsEmpty() {
        when(trainingTypeRepository.findAll()).thenReturn(List.of());

        dataInitializer.init();

        ArgumentCaptor<TrainingType> captor =
                ArgumentCaptor.forClass(TrainingType.class);

        verify(trainingTypeRepository).findAll();
        verify(trainingTypeRepository, times(5)).save(captor.capture());
        verifyNoMoreInteractions(trainingTypeRepository);

        List<String> savedNames = captor.getAllValues()
                .stream()
                .map(TrainingType::getName)
                .toList();

        assertThat(savedNames)
                .containsExactly(
                        "Fitness",
                        "Yoga",
                        "Zumba",
                        "Stretching",
                        "Resistance"
                );
    }
}
