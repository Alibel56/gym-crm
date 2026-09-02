package com.practice.workloadservice.service;

import com.practice.testtags.PositiveTest;
import com.practice.testtags.NegativeTest;
import com.practice.testtags.EdgeCaseTest;
import com.practice.testtags.ExceptionTest;

import com.practice.workloadservice.dto.ActionType;
import com.practice.workloadservice.dto.MonthlyWorkloadResponse;
import com.practice.workloadservice.dto.TrainerWorkloadRequest;
import com.practice.workloadservice.dto.TrainerWorkloadResponse;
import com.practice.workloadservice.exception.WorkloadNotFoundException;
import com.practice.workloadservice.model.MonthEntry;
import com.practice.workloadservice.model.TrainerWorkloadDocument;
import com.practice.workloadservice.model.YearEntry;
import com.practice.workloadservice.repository.TrainerWorkloadDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    @Mock
    private TrainerWorkloadDocumentRepository repository;

    private TrainerWorkloadService service;

    private TrainerWorkloadService newService() {
        return new TrainerWorkloadService(repository);
    }

    @Test
    @NegativeTest
    void update_shouldCreateNewDocument_whenTrainerDoesNotExist() {
        service = newService();
        when(repository.findByUsername("john.doe")).thenReturn(Optional.empty());

        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "john.doe", "John", "Doe", true,
                LocalDate.of(2026, 3, 15), 60, ActionType.ADD);

        service.update(request);

        ArgumentCaptor<TrainerWorkloadDocument> captor = ArgumentCaptor.forClass(TrainerWorkloadDocument.class);
        verify(repository).save(captor.capture());

        TrainerWorkloadDocument saved = captor.getValue();
        assertEquals("john.doe", saved.getTrainerUsername());
        assertEquals("John", saved.getTrainerFirstName());
        assertEquals("Doe", saved.getTrainerLastName());
        assertTrue(saved.getTrainerStatus());
        assertEquals(1, saved.getYears().size());
        YearEntry year = saved.getYears().get(0);
        assertEquals(2026, year.getYear());
        assertEquals(1, year.getMonths().size());
        assertEquals(3, year.getMonths().get(0).getMonth());
        assertEquals(60, year.getMonths().get(0).getTrainingsSummaryDuration());
    }

    @Test
    @PositiveTest
    void update_shouldAddDurationToExistingMonth_whenRecordExists() {
        service = newService();
        TrainerWorkloadDocument existing = new TrainerWorkloadDocument("john.doe", "John", "Doe", true);
        YearEntry year = new YearEntry(2026);
        year.getMonths().add(new MonthEntry(3, 40));
        existing.getYears().add(year);

        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existing));

        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "john.doe", "John", "Doe", true,
                LocalDate.of(2026, 3, 20), 30, ActionType.ADD);

        service.update(request);

        ArgumentCaptor<TrainerWorkloadDocument> captor = ArgumentCaptor.forClass(TrainerWorkloadDocument.class);
        verify(repository).save(captor.capture());
        assertEquals(70, captor.getValue().getYears().get(0).getMonths().get(0).getTrainingsSummaryDuration());
    }

    @Test
    @PositiveTest
    void update_shouldCreateNewMonth_whenYearExistsButMonthDoesNot() {
        service = newService();
        TrainerWorkloadDocument existing = new TrainerWorkloadDocument("john.doe", "John", "Doe", true);
        YearEntry year = new YearEntry(2026);
        year.getMonths().add(new MonthEntry(3, 40));
        existing.getYears().add(year);

        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existing));

        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "john.doe", "John", "Doe", true,
                LocalDate.of(2026, 4, 1), 25, ActionType.ADD);

        service.update(request);

        ArgumentCaptor<TrainerWorkloadDocument> captor = ArgumentCaptor.forClass(TrainerWorkloadDocument.class);
        verify(repository).save(captor.capture());
        YearEntry savedYear = captor.getValue().getYears().get(0);
        assertEquals(2, savedYear.getMonths().size());
    }

    @Test
    @PositiveTest
    void update_shouldSubtractDuration_whenActionIsDelete() {
        service = newService();
        TrainerWorkloadDocument existing = new TrainerWorkloadDocument("john.doe", "John", "Doe", true);
        YearEntry year = new YearEntry(2026);
        year.getMonths().add(new MonthEntry(3, 40));
        existing.getYears().add(year);

        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existing));

        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "john.doe", "John", "Doe", true,
                LocalDate.of(2026, 3, 20), 15, ActionType.DELETE);

        service.update(request);

        ArgumentCaptor<TrainerWorkloadDocument> captor = ArgumentCaptor.forClass(TrainerWorkloadDocument.class);
        verify(repository).save(captor.capture());
        assertEquals(25, captor.getValue().getYears().get(0).getMonths().get(0).getTrainingsSummaryDuration());
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    @ExceptionTest
    void update_shouldThrow_andNotSave_whenResultingDurationIsNegative() {
        service = newService();
        TrainerWorkloadDocument existing = new TrainerWorkloadDocument("john.doe", "John", "Doe", true);
        YearEntry year = new YearEntry(2026);
        year.getMonths().add(new MonthEntry(3, 10));
        existing.getYears().add(year);

        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existing));

        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "john.doe", "John", "Doe", true,
                LocalDate.of(2026, 3, 20), 15, ActionType.DELETE);

        assertThrows(IllegalArgumentException.class, () -> service.update(request));
        verify(repository, never()).save(any());
    }

    @Test
    @PositiveTest
    void update_shouldRefreshTrainerIdentity_whenRecordExists() {
        service = newService();
        TrainerWorkloadDocument existing = new TrainerWorkloadDocument("john.doe", "Old", "Name", false);
        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existing));

        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "john.doe", "John", "Doe", true,
                LocalDate.of(2026, 1, 1), 10, ActionType.ADD);

        service.update(request);

        ArgumentCaptor<TrainerWorkloadDocument> captor = ArgumentCaptor.forClass(TrainerWorkloadDocument.class);
        verify(repository).save(captor.capture());
        assertEquals("John", captor.getValue().getTrainerFirstName());
        assertEquals("Doe", captor.getValue().getTrainerLastName());
        assertTrue(captor.getValue().getTrainerStatus());
    }

    @Test
    @PositiveTest
    void getByUsername_shouldReturnSortedYearsAndMonths() {
        service = newService();
        TrainerWorkloadDocument doc = new TrainerWorkloadDocument("john.doe", "John", "Doe", true);
        YearEntry year2027 = new YearEntry(2027);
        year2027.getMonths().add(new MonthEntry(5, 10));
        YearEntry year2026 = new YearEntry(2026);
        year2026.getMonths().add(new MonthEntry(12, 20));
        year2026.getMonths().add(new MonthEntry(1, 5));
        doc.getYears().add(year2027);
        doc.getYears().add(year2026);

        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(doc));

        TrainerWorkloadResponse response = service.getByUsername("john.doe");

        assertEquals(2026, response.years().get(0).year());
        assertEquals(1, response.years().get(0).months().get(0).month());
        assertEquals(12, response.years().get(0).months().get(1).month());
        assertEquals(2027, response.years().get(1).year());
    }

    @Test
    @NegativeTest
    @ExceptionTest
    void getByUsername_shouldThrow_whenNotFound() {
        service = newService();
        when(repository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(WorkloadNotFoundException.class, () -> service.getByUsername("missing"));
    }

    @Test
    @EdgeCaseTest
    void getMonthly_shouldReturnZero_whenNoDataForMonth() {
        service = newService();
        TrainerWorkloadDocument doc = new TrainerWorkloadDocument("john.doe", "John", "Doe", true);
        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(doc));

        MonthlyWorkloadResponse response = service.getMonthly("john.doe", 2026, 3);

        assertEquals(0, response.trainingSummaryDuration());
    }

    @Test
    @NegativeTest
    @EdgeCaseTest
    @ExceptionTest
    void getMonthly_shouldThrow_whenMonthOutOfRange() {
        service = newService();
        assertThrows(IllegalArgumentException.class, () -> service.getMonthly("john.doe", 2026, 13));
    }
}
