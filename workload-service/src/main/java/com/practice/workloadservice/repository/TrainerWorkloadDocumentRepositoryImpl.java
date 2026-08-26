package com.practice.workloadservice.repository;

import com.practice.workloadservice.model.TrainerWorkloadDocument;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TrainerWorkloadDocumentRepositoryImpl implements TrainerWorkloadDocumentRepository {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadDocumentRepositoryImpl.class);
    private static final String TRAINER_NAME_INDEX = "trainer_name_idx";

    private final MongoTemplate mongoTemplate;

    public TrainerWorkloadDocumentRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    void ensureIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps(TrainerWorkloadDocument.class);
        indexOps.ensureIndex(new CompoundIndexDefinition(
                new Document("trainerFirstName", 1).append("trainerLastName", 1))
                .named(TRAINER_NAME_INDEX));
        log.info("op=ensureIndexes index={} collection={} - ensured", TRAINER_NAME_INDEX,
                TrainerWorkloadDocument.COLLECTION_NAME);
    }

    @Override
    public Optional<TrainerWorkloadDocument> findByUsername(String username) {
        log.debug("op=findByUsername username={} - searching document", username);
        TrainerWorkloadDocument document = mongoTemplate.findById(username, TrainerWorkloadDocument.class);
        log.debug("op=findByUsername username={} found={}", username, document != null);
        return Optional.ofNullable(document);
    }

    @Override
    public TrainerWorkloadDocument save(TrainerWorkloadDocument document) {
        log.debug("op=save username={} - persisting document", document.getTrainerUsername());
        TrainerWorkloadDocument saved = mongoTemplate.save(document);
        log.debug("op=save username={} - persisted", document.getTrainerUsername());
        return saved;
    }
}
