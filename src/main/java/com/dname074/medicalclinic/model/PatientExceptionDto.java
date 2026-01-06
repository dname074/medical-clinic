package com.dname074.medicalclinic.model;

import org.springframework.http.HttpStatus;

public record PatientExceptionDto(String message, HttpStatus status) {
}
