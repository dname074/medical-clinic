package com.dname074.medicalclinic.exception.institution;

import com.dname074.medicalclinic.exception.MedicalClinicException;
import org.springframework.http.HttpStatus;

public class InstitutionExistsException extends MedicalClinicException {
    public InstitutionExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
