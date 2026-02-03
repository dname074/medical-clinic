package com.dname074.medicalclinic.exception.institution;

import com.dname074.medicalclinic.exception.MedicalClinicException;
import org.springframework.http.HttpStatus;

public class InstitutionNotFoundException extends MedicalClinicException {
    public InstitutionNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
