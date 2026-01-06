package com.dname074.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class PatientAlreadyExistsException extends MedicalClinicException {
    public PatientAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
