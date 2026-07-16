package com.practice.service;

import com.practice.dao.TrainingTypeRepository;
import com.practice.dto.response.TrainingTypesSummaryResponse;
import com.practice.mapper.response.TrainingTypesSummaryResponseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TrainingTypeService {
    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private TrainingTypeRepository trainingTypeRepository;
    private TrainingTypesSummaryResponseMapper trainingTypesSummaryResponseMapper;

    @Autowired
    public void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Autowired
    public void setTrainingTypesSummaryResponseMapper(TrainingTypesSummaryResponseMapper trainingTypesSummaryResponseMapper) {
        this.trainingTypesSummaryResponseMapper = trainingTypesSummaryResponseMapper;
    }

    public List<TrainingTypesSummaryResponse> findAll(){
        return trainingTypeRepository.findAll().stream()
                .map(trainingTypesSummaryResponseMapper::mapFrom)
                .collect(Collectors.toList());
    }

}
