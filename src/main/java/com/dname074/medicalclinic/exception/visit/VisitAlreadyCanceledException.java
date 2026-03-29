package com.dname074.medicalclinic.exception.visit;

import com.dname074.medicalclinic.exception.MedicalClinicException;
import org.springframework.http.HttpStatus;

public class VisitAlreadyCanceledException extends MedicalClinicException {
    public VisitAlreadyCanceledException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
