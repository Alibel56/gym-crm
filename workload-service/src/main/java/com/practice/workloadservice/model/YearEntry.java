package com.practice.workloadservice.model;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * Embedded document representing a single year within a {@link TrainerWorkloadDocument}.
 * Holds the "Months List" as described by the follow-functionality schema.
 */
public class YearEntry {

    @Field("year")
    private int year;

    @Field("months")
    private List<MonthEntry> months = new ArrayList<>();

    protected YearEntry() {
        // required by the Mongo converter
    }

    public YearEntry(int year) {
        this.year = year;
        this.months = new ArrayList<>();
    }

    public int getYear() {
        return year;
    }

    public List<MonthEntry> getMonths() {
        return months;
    }
}
