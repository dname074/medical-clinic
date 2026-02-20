package com.dname074.medicalclinic.validation;

import com.dname074.medicalclinic.exception.visit.InvalidVisitException;
import com.dname074.medicalclinic.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class VisitValidatorTest {
    VisitRepository visitRepository;
    VisitValidator validator;
    Clock clock = Clock.fixed(
            LocalDateTime.of(2026, 2, 15, 12, 0, 0)
                    .atZone(ZoneId.systemDefault())
                    .toInstant(),
            ZoneId.systemDefault()
    );

    @BeforeEach
    void setup() {
        this.visitRepository = Mockito.mock(VisitRepository.class);
        this.validator = new VisitValidator(this.visitRepository, this.clock);
    }

    @Test
    void validateVisitDate_DataCorrect_ContinueWithoutException() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2026, 12, 1, 15, 30, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 12, 1, 16, 30, 0);
        when(visitRepository.existsByStartDateLessThanAndEndDateGreaterThan(endDate, startDate)).thenReturn(false);
        // when
        validator.validateVisitDate(startDate, endDate);
        // then
        verify(visitRepository, times(1)).existsByStartDateLessThanAndEndDateGreaterThan(
                argThat(new LocalDateTimeArgumentMatcher(LocalDateTime.of(2026, 12, 1, 16, 30, 0))),
                argThat(new LocalDateTimeArgumentMatcher(LocalDateTime.of(2026, 12, 1, 15, 30, 0)))
        );
        verifyNoMoreInteractions(visitRepository);
    }

    @Test
    void validateVisitDate_StartDateAfterEndDate_InvalidVisitExceptionThrown() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2026, 12, 1, 16, 30, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 12, 1, 15, 30, 0);
        // when & then
        InvalidVisitException exception = assertThrows(InvalidVisitException.class, () -> validator.validateVisitDate(startDate, endDate));
        assertEquals("Data początkowa wizyty nie może być po dacie końcowej", exception.getMessage());
        verifyNoInteractions(visitRepository);
    }

    @Test
    void validateVisitDate_StartDateEqualsEndDate_InvalidVisitExceptionThrown() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2026, 12, 1, 15, 30, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 12, 1, 15, 30, 0);
        // when & then
        InvalidVisitException exception = assertThrows(InvalidVisitException.class, () -> validator.validateVisitDate(startDate, endDate));
        assertEquals("Data początkowa wizyty nie może być taka sama jak data końcowa", exception.getMessage());
        verifyNoInteractions(visitRepository);
    }

    @Test
    void validateVisitDate_VisitDateNotInFullQuarterOfAnHour_InvalidVisitExceptionThrown() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2026, 12, 1, 15, 32, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 12, 1, 16, 41, 0);
        // when & then
        InvalidVisitException exception = assertThrows(InvalidVisitException.class, () -> validator.validateVisitDate(startDate, endDate));
        assertEquals("Godziny wizyt muszą być w pełnym kwadransie godziny", exception.getMessage());
        verifyNoInteractions(visitRepository);
    }

    @Test
    void validateVisitDate_StartDateBeforeCurrentDate_InvalidVisitExceptionThrown() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2026, 1, 1, 15, 30, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 1, 1, 16, 30, 0);
        // when & then
        InvalidVisitException exception = assertThrows(InvalidVisitException.class, () -> validator.validateVisitDate(startDate, endDate));
        assertEquals("Data wizyty nie może poprzedzać aktualnej daty", exception.getMessage());
        verifyNoInteractions(visitRepository);
    }

    @Test
    void validateVisitDate_VisitDateTaken_InvalidVisitExceptionThrown() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2026, 12, 1, 14, 30, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 12, 1, 15, 30, 0);
        when(visitRepository.existsByStartDateLessThanAndEndDateGreaterThan(endDate, startDate)).thenReturn(true);
        // when & then
        InvalidVisitException exception = assertThrows(InvalidVisitException.class, () -> validator.validateVisitDate(startDate, endDate));
        assertEquals("Data wizyty pokrywa się z już istniejącą", exception.getMessage());
        verify(visitRepository, times(1)).existsByStartDateLessThanAndEndDateGreaterThan(endDate, startDate);
        verifyNoMoreInteractions(visitRepository);
    }

    @RequiredArgsConstructor
    public static class LocalDateTimeArgumentMatcher implements ArgumentMatcher<LocalDateTime> {
        private final LocalDateTime localDateTime;

        @Override
        public boolean matches(LocalDateTime newLocalDateTime) {
            return nonNull(localDateTime) && this.localDateTime.isEqual(newLocalDateTime);
        }
    }
}
