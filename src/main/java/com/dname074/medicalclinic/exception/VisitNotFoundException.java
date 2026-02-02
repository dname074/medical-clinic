package com.dname074.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class VisitNotFoundException extends MedicalClinicException {
    public VisitNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
