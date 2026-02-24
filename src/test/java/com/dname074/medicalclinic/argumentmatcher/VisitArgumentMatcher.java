package com.dname074.medicalclinic.argumentmatcher;

import com.dname074.medicalclinic.model.Visit;
import lombok.RequiredArgsConstructor;
import org.mockito.ArgumentMatcher;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class VisitArgumentMatcher implements ArgumentMatcher<Visit> {
    private final Visit visit;

    @Override
    public boolean matches(Visit newVisit) {
        return nonNull(visit) && this.visit.getStartDate().equals(newVisit.getStartDate()) &&
                this.visit.getEndDate().equals(newVisit.getEndDate()) &&
                (this.visit.getDoctor()==newVisit.getDoctor() ||
                        this.visit.getDoctor().getId().equals(newVisit.getDoctor().getId())) &&
                (this.visit.getPatient()==newVisit.getPatient() ||
                        this.visit.getPatient().getId().equals(newVisit.getPatient().getId()));
    }
}
