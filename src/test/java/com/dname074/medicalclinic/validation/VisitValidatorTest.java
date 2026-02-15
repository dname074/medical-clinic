package com.dname074.medicalclinic.validation;

import com.dname074.medicalclinic.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static java.util.Objects.nonNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class VisitValidatorTest {
    VisitRepository visitRepository;
    VisitValidator validator;

    @BeforeEach
    void setup() {
        this.visitRepository = Mockito.mock(VisitRepository.class);
        this.validator = new VisitValidator(visitRepository);
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

    @RequiredArgsConstructor
    public static class LocalDateTimeArgumentMatcher implements ArgumentMatcher<LocalDateTime> {
        private final LocalDateTime localDateTime;

        @Override
        public boolean matches(LocalDateTime newLocalDateTime) {
            return nonNull(localDateTime) && this.localDateTime.isEqual(newLocalDateTime);
        }
    }
}
