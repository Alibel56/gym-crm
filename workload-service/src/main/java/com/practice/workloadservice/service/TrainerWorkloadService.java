package com.practice.workloadservice.service;

import com.practice.workloadservice.dto.ActionType;
import com.practice.workloadservice.dto.MonthSummaryResponse;
import com.practice.workloadservice.dto.MonthlyWorkloadResponse;
import com.practice.workloadservice.dto.TrainerWorkloadRequest;
import com.practice.workloadservice.dto.TrainerWorkloadResponse;
import com.practice.workloadservice.dto.YearSummaryResponse;
import com.practice.workloadservice.exception.WorkloadNotFoundException;
import com.practice.workloadservice.model.MonthEntry;
import com.practice.workloadservice.model.TrainerWorkloadDocument;
import com.practice.workloadservice.model.YearEntry;
import com.practice.workloadservice.repository.TrainerWorkloadDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Service
public class TrainerWorkloadService {

    private static final Logger transactionLog = LoggerFactory.getLogger("com.practice.workload.transaction");
    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadService.class);
    private static final String TRANSACTION_ID_MDC_KEY = "transactionId";
    private static final String UNKNOWN_TRANSACTION_ID = "unknown";

    private final TrainerWorkloadDocumentRepository repository;
    private final ConcurrentMap<String, Object> perTrainerLocks = new ConcurrentHashMap<>();

    public TrainerWorkloadService(TrainerWorkloadDocumentRepository repository) {
        this.repository = repository;
    }

    public void update(TrainerWorkloadRequest request) {
        String transactionId = currentTransactionId();
        String username = request.trainerUsername();

        transactionLog.info("Transaction started: process workload event trainer={} action={} txId={}",
                username, request.actionType(), transactionId);

        Object lock = perTrainerLocks.computeIfAbsent(username, ignored -> new Object());
        synchronized (lock) {
            try {
                // a) extract trainer's record by username
                log.debug("op=extractTrainerRecord username={} txId={}", username, transactionId);
                Optional<TrainerWorkloadDocument> existing = repository.findByUsername(username);

                TrainerWorkloadDocument document;
                if (existing.isEmpty()) {
                    // b) create new record, Year/Month derived from the training date
                    log.debug("op=createTrainerRecord username={} txId={} reason=notFound", username, transactionId);
                    document = new TrainerWorkloadDocument(
                            username,
                            request.trainerFirstName(),
                            request.trainerLastName(),
                            request.isActive());
                } else {
                    // c) trainer profile extracted successfully
                    log.debug("op=refreshTrainerRecord username={} txId={}", username, transactionId);
                    document = existing.get();
                    document.setTrainerFirstName(request.trainerFirstName());
                    document.setTrainerLastName(request.trainerLastName());
                    document.setTrainerStatus(request.isActive());
                }

                // c/d) find (or create) the correct Year/Month record and add the duration to it
                applyTrainingDuration(document, request, transactionId);

                // e) save new value in the Mongo DB document
                log.debug("op=saveTrainerRecord username={} txId={}", username, transactionId);
                repository.save(document);

                transactionLog.info("Transaction finished: workload updated trainer={} txId={}",
                        username, transactionId);
            } catch (RuntimeException ex) {
                transactionLog.warn("Transaction failed: trainer={} txId={} error={}",
                        username, transactionId, ex.getMessage());
                throw ex;
            }
        }
    }

    private void applyTrainingDuration(TrainerWorkloadDocument document,
                                       TrainerWorkloadRequest request,
                                       String transactionId) {
        LocalDate trainingDate = request.trainingDate();
        int year = trainingDate.getYear();
        int month = trainingDate.getMonthValue();
        int delta = request.actionType() == ActionType.ADD
                ? request.trainingDuration()
                : -request.trainingDuration();

        YearEntry yearEntry = findOrCreateYear(document, year, transactionId);
        MonthEntry monthEntry = findOrCreateMonth(document, yearEntry, month, transactionId);

        int updatedDuration = monthEntry.getTrainingsSummaryDuration() + delta;
        if (updatedDuration < 0) {
            log.warn("op=applyTrainingDuration username={} year={} month={} txId={} rejected result={}",
                    document.getTrainerUsername(), year, month, transactionId, updatedDuration);
            throw new IllegalArgumentException("Training summary duration cannot become negative");
        }

        log.debug("op=applyTrainingDuration username={} year={} month={} txId={} {} -> {}",
                document.getTrainerUsername(), year, month, transactionId,
                monthEntry.getTrainingsSummaryDuration(), updatedDuration);
        monthEntry.setTrainingsSummaryDuration(updatedDuration);
    }

    private YearEntry findOrCreateYear(TrainerWorkloadDocument document, int year, String transactionId) {
        return document.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("op=createYearEntry username={} year={} txId={}",
                            document.getTrainerUsername(), year, transactionId);
                    YearEntry created = new YearEntry(year);
                    document.getYears().add(created);
                    return created;
                });
    }

    private MonthEntry findOrCreateMonth(TrainerWorkloadDocument document, YearEntry yearEntry, int month,
                                         String transactionId) {
        return yearEntry.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("op=createMonthEntry username={} year={} month={} txId={}",
                            document.getTrainerUsername(), yearEntry.getYear(), month, transactionId);
                    MonthEntry created = new MonthEntry(month, 0);
                    yearEntry.getMonths().add(created);
                    return created;
                });
    }

    public TrainerWorkloadResponse getByUsername(String username) {
        TrainerWorkloadDocument document = find(username);
        List<YearSummaryResponse> years = document.getYears().stream()
                .sorted((a, b) -> Integer.compare(a.getYear(), b.getYear()))
                .map(year -> new YearSummaryResponse(
                        year.getYear(),
                        year.getMonths().stream()
                                .sorted((a, b) -> Integer.compare(a.getMonth(), b.getMonth()))
                                .map(m -> new MonthSummaryResponse(m.getMonth(), m.getTrainingsSummaryDuration()))
                                .toList()))
                .toList();

        return new TrainerWorkloadResponse(
                document.getTrainerUsername(),
                document.getTrainerFirstName(),
                document.getTrainerLastName(),
                document.getTrainerStatus(),
                years);
    }

    public MonthlyWorkloadResponse getMonthly(String username, int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        TrainerWorkloadDocument document = find(username);
        int duration = document.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .flatMap(y -> y.getMonths().stream()
                        .filter(m -> m.getMonth() == month)
                        .findFirst())
                .map(MonthEntry::getTrainingsSummaryDuration)
                .orElse(0);
        return new MonthlyWorkloadResponse(username, year, month, duration);
    }

    private TrainerWorkloadDocument find(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new WorkloadNotFoundException("Trainer workload not found: " + username));
    }

    private String currentTransactionId() {
        String transactionId = MDC.get(TRANSACTION_ID_MDC_KEY);
        return transactionId != null ? transactionId : UNKNOWN_TRANSACTION_ID;
    }
}
