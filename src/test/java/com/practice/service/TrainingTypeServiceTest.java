package com.practice.service;

import com.practice.dao.TrainingTypeRepository;
import com.practice.dto.response.TrainingTypesSummaryResponse;
import com.practice.entity.TrainingType;
import com.practice.mapper.response.TrainingTypesSummaryResponseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private TrainingTypesSummaryResponseMapper trainingTypesSummaryResponseMapper;

    private TrainingTypeService trainingTypeService;

    @BeforeEach
    void setUp() {
        trainingTypeService = new TrainingTypeService();
        trainingTypeService.setTrainingTypeRepository(trainingTypeRepository);
        trainingTypeService.setTrainingTypesSummaryResponseMapper(
                trainingTypesSummaryResponseMapper
        );
    }

    @Test
    void findAll_shouldReturnMappedTrainingTypes() {
        
        TrainingType cardio = TrainingType.builder()
                .id(1L)
                .name("Cardio")
                .build();

        TrainingType yoga = TrainingType.builder()
                .id(2L)
                .name("Yoga")
                .build();

        TrainingTypesSummaryResponse cardioResponse =
                mock(TrainingTypesSummaryResponse.class);

        TrainingTypesSummaryResponse yogaResponse =
                mock(TrainingTypesSummaryResponse.class);

        when(trainingTypeRepository.findAll())
                .thenReturn(List.of(cardio, yoga));

        when(trainingTypesSummaryResponseMapper.mapFrom(cardio))
                .thenReturn(cardioResponse);

        when(trainingTypesSummaryResponseMapper.mapFrom(yoga))
                .thenReturn(yogaResponse);

        
        List<TrainingTypesSummaryResponse> result =
                trainingTypeService.findAll();

        
        assertThat(result)
                .containsExactly(cardioResponse, yogaResponse);

        verify(trainingTypeRepository).findAll();
        verify(trainingTypesSummaryResponseMapper).mapFrom(cardio);
        verify(trainingTypesSummaryResponseMapper).mapFrom(yoga);
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoTrainingTypesExist() {
        
        when(trainingTypeRepository.findAll())
                .thenReturn(List.of());

        
        List<TrainingTypesSummaryResponse> result =
                trainingTypeService.findAll();

        
        assertThat(result).isEmpty();

        verify(trainingTypeRepository).findAll();
        verifyNoInteractions(trainingTypesSummaryResponseMapper);
    }
}