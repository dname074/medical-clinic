package com.dname074.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class DateAlreadyOccupiedException extends MedicalClinicException {
    public DateAlreadyOccupiedException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
