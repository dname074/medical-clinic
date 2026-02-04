package com.dname074.medicalclinic.exception.visit;

import com.dname074.medicalclinic.exception.MedicalClinicException;
import org.springframework.http.HttpStatus;

public class VisitExpiredException extends MedicalClinicException {
    public VisitExpiredException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
