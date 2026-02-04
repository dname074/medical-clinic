package com.dname074.medicalclinic.exception.visit;

import com.dname074.medicalclinic.exception.MedicalClinicException;
import org.springframework.http.HttpStatus;

public class VisitNotFoundException extends MedicalClinicException {
    public VisitNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
