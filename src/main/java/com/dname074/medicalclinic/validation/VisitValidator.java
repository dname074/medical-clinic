package com.dname074.medicalclinic.validation;

import com.dname074.medicalclinic.exception.visit.InvalidVisitException;
import com.dname074.medicalclinic.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class VisitValidator {
    private final VisitRepository visitRepository;

    public void validateVisitDate(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidVisitException("Data początkowa wizyty nie może być po dacie końcowej");
        }
        if (startDate.isEqual(endDate)) {
            throw new InvalidVisitException("Data początkowa wizyty nie może być taka sama jak data końcowa");
        }
        if (startDate.getMinute() % 15 != 0 || endDate.getMinute() % 15 != 0 || startDate.getSecond() != 0 || endDate.getSecond() != 0) {
            throw new InvalidVisitException("Godziny wizyt muszą być w pełnym kwadransie godziny");
        }
        if (startDate.isBefore(LocalDateTime.now())) {
            throw new InvalidVisitException("Data wizyty nie może poprzedzać aktualnej daty");
        }
        if (visitRepository.existsByStartDateLessThanAndEndDateGreaterThan(endDate, startDate)) {
            throw new InvalidVisitException("Data wizyty pokrywa się z już istniejącą");
        }
    }
}
