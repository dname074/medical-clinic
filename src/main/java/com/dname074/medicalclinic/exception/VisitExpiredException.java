package com.dname074.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class VisitExpiredException extends MedicalClinicException {
    public VisitExpiredException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
