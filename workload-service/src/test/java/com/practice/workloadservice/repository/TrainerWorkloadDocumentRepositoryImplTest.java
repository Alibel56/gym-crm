package com.practice.workloadservice.repository;

import com.practice.workloadservice.model.MonthEntry;
import com.practice.workloadservice.model.TrainerWorkloadDocument;
import com.practice.workloadservice.model.YearEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest
@Import({TrainerWorkloadDocumentRepositoryImpl.class, EmbeddedMongoAutoConfiguration.class})
class TrainerWorkloadDocumentRepositoryImplTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TrainerWorkloadDocumentRepositoryImpl repository;

    @Test
    void save_thenFindByUsername_shouldReturnPersistedDocument() {
        TrainerWorkloadDocument document = new TrainerWorkloadDocument(
                "jane.smith", "Jane", "Smith", true);
        YearEntry year = new YearEntry(2026);
        year.getMonths().add(new MonthEntry(6, 45));
        document.getYears().add(year);

        repository.save(document);

        Optional<TrainerWorkloadDocument> found = repository.findByUsername("jane.smith");

        assertTrue(found.isPresent());
        assertEquals("Jane", found.get().getTrainerFirstName());
        assertEquals("Smith", found.get().getTrainerLastName());
        assertEquals(45, found.get().getYears().get(0).getMonths().get(0).getTrainingsSummaryDuration());
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenDocumentDoesNotExist() {
        Optional<TrainerWorkloadDocument> found = repository.findByUsername("does.not.exist");

        assertTrue(found.isEmpty());
    }

    @Test
    void save_shouldUpsertExistingDocument() {
        TrainerWorkloadDocument document = new TrainerWorkloadDocument(
                "upsert.user", "First", "Last", true);
        repository.save(document);

        TrainerWorkloadDocument updated = repository.findByUsername("upsert.user").orElseThrow();
        updated.setTrainerFirstName("Updated");
        repository.save(updated);

        List<TrainerWorkloadDocument> all = mongoTemplate.findAll(TrainerWorkloadDocument.class);
        long matching = all.stream()
                .filter(d -> d.getTrainerUsername().equals("upsert.user"))
                .count();

        assertEquals(1, matching);
        assertEquals("Updated", repository.findByUsername("upsert.user").orElseThrow().getTrainerFirstName());
    }

    @Test
    void ensureIndexes_shouldCreateCompoundIndexOnFirstAndLastName() {
        List<IndexInfo> indexes = mongoTemplate.indexOps(TrainerWorkloadDocument.class).getIndexInfo();

        assertTrue(indexes.stream().anyMatch(i -> i.getName().equals("trainer_name_idx")));
    }
}
