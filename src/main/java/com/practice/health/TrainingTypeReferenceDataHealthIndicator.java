package com.practice.health;

import com.practice.dto.response.TrainingTypesSummaryResponse;
import com.practice.service.TrainingTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("trainingTypeReferenceData")
public class TrainingTypeReferenceDataHealthIndicator implements HealthIndicator {

    private static final Logger log =
            LoggerFactory.getLogger(TrainingTypeReferenceDataHealthIndicator.class);

    private TrainingTypeService trainingTypeService;

    @Autowired
    public void setTrainingTypeService(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    @Override
    public Health health() {
        try {
            List<TrainingTypesSummaryResponse> trainingTypes = trainingTypeService.findAll();

            if (trainingTypes.isEmpty()) {
                log.warn("Health check: no training types found");

                return Health.down()
                        .withDetail("reason", "No training types configured")
                        .build();
            }

            return Health.up()
                    .withDetail("trainingTypesCount", trainingTypes.size())
                    .build();

        } catch (Exception ex) {
            log.error("Health check: failed to read training types", ex);

            return Health.down()
                    .withException(ex)
                    .build();
        }
    }
}