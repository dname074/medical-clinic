package com.dname074.medicalclinic.exception;

import org.springframework.http.HttpStatus;

public class DoctorAlreadyExistsException extends MedicalClinicException {
    public DoctorAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
