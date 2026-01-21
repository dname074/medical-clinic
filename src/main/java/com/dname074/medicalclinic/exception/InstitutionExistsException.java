package com.dname074.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class InstitutionExistsException extends MedicalClinicException {
    public InstitutionExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
