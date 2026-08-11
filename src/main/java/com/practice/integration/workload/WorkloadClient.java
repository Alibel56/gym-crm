package com.practice.integration.workload;

import com.practice.entity.Training;
import com.practice.entity.User;
import com.practice.logging.TransactionIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WorkloadClient {

    private static final Logger log = LoggerFactory.getLogger(WorkloadClient.class);
    private static final String WORKLOAD_SERVICE_URL = "http://workload-service/api/v1/workloads";

    private final RestClient restClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final ServiceTokenProvider serviceTokenProvider;

    public WorkloadClient(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            CircuitBreakerFactory<?, ?> circuitBreakerFactory,
            ServiceTokenProvider serviceTokenProvider) {
        this.restClient = restClientBuilder.build();
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.serviceTokenProvider = serviceTokenProvider;
    }

    public boolean updateWorkload(Training training, WorkloadActionType actionType) {
        User trainerUser = training.getTrainer().getUser();
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                trainerUser.getUserName(),
                trainerUser.getFirstName(),
                trainerUser.getLastName(),
                trainerUser.getIsActive(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                actionType
        );

        return circuitBreakerFactory.create("workloadService").run(
                () -> send(request),
                throwable -> fallback(request, throwable)
        );
    }

    private boolean send(TrainerWorkloadRequest request) {
        restClient.post()
                .uri(WORKLOAD_SERVICE_URL)
                .header(TransactionIdFilter.HEADER_NAME, currentTransactionId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceTokenProvider.issueToken())
                .body(request)
                .retrieve()
                .toBodilessEntity();

        log.info("Workload service updated trainer={} action={}", request.trainerUsername(), request.actionType());
        return true;
    }

    private boolean fallback(TrainerWorkloadRequest request, Throwable throwable) {
        log.error("Workload service unavailable trainer={} action={} error={}",
                request.trainerUsername(), request.actionType(), throwable.getMessage());
        return false;
    }

    private String currentTransactionId() {
        String transactionId = MDC.get(TransactionIdFilter.MDC_KEY);
        return transactionId == null ? "" : transactionId;
    }
}