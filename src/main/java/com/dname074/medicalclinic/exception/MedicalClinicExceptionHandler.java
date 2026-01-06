package com.dname074.medicalclinic.exception;

import com.dname074.medicalclinic.model.PatientExceptionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MedicalClinicExceptionHandler {

    @ExceptionHandler(MedicalClinicException.class)
    ResponseEntity<PatientExceptionDto> handleMedicalClinicException(MedicalClinicException exception) {
        return ResponseEntity.status(exception.getStatus()).body(new PatientExceptionDto(exception.getMessage(), exception.getStatus()));
    }
}
