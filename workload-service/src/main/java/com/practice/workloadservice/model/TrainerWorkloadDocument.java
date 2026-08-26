package com.practice.workloadservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Document(collection = TrainerWorkloadDocument.COLLECTION_NAME)
@CompoundIndexes({
        @CompoundIndex(
                name = "trainer_name_idx",
                def = "{'trainerFirstName': 1, 'trainerLastName': 1}"
        )
})
public class TrainerWorkloadDocument {

    public static final String COLLECTION_NAME = "trainer_workloads";

    @Id
    private String trainerUsername;

    @Field("trainerFirstName")
    private String trainerFirstName;

    @Field("trainerLastName")
    private String trainerLastName;

    @Field("trainerStatus")
    private Boolean trainerStatus;

    @Field("years")
    private List<YearEntry> years = new ArrayList<>();

    protected TrainerWorkloadDocument() {
        // required by the Mongo converter
    }

    public TrainerWorkloadDocument(String trainerUsername,
                                    String trainerFirstName,
                                    String trainerLastName,
                                    Boolean trainerStatus) {
        this.trainerUsername = trainerUsername;
        this.trainerFirstName = trainerFirstName;
        this.trainerLastName = trainerLastName;
        this.trainerStatus = trainerStatus;
        this.years = new ArrayList<>();
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public String getTrainerFirstName() {
        return trainerFirstName;
    }

    public void setTrainerFirstName(String trainerFirstName) {
        this.trainerFirstName = trainerFirstName;
    }

    public String getTrainerLastName() {
        return trainerLastName;
    }

    public void setTrainerLastName(String trainerLastName) {
        this.trainerLastName = trainerLastName;
    }

    public Boolean getTrainerStatus() {
        return trainerStatus;
    }

    public void setTrainerStatus(Boolean trainerStatus) {
        this.trainerStatus = trainerStatus;
    }

    public List<YearEntry> getYears() {
        return years;
    }
}
