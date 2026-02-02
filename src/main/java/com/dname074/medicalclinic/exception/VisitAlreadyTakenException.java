package com.dname074.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class VisitAlreadyTakenException extends MedicalClinicException {
    public VisitAlreadyTakenException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
