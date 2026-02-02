package com.dname074.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class InvalidVisitException extends MedicalClinicException {
    public InvalidVisitException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
