package com.dname074.medicalclinic.exception.visit;

import com.dname074.medicalclinic.exception.MedicalClinicException;
import org.springframework.http.HttpStatus;

public class VisitAlreadyTakenException extends MedicalClinicException {
    public VisitAlreadyTakenException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
