package com.practice.workloadservice.model;

import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Embedded document representing a single month within a {@link YearEntry}.
 * "Trainings summary duration" is required to have a number type.
 */
public class MonthEntry {

    @Field("month")
    private int month;

    @Field("trainingsSummaryDuration")
    private int trainingsSummaryDuration;

    protected MonthEntry() {
        // required by the Mongo converter
    }

    public MonthEntry(int month, int trainingsSummaryDuration) {
        this.month = month;
        this.trainingsSummaryDuration = trainingsSummaryDuration;
    }

    public int getMonth() {
        return month;
    }

    public int getTrainingsSummaryDuration() {
        return trainingsSummaryDuration;
    }

    public void setTrainingsSummaryDuration(int trainingsSummaryDuration) {
        this.trainingsSummaryDuration = trainingsSummaryDuration;
    }
}
