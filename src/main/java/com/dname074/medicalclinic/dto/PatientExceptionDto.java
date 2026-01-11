package com.dname074.medicalclinic.dto;

import org.springframework.http.HttpStatus;

public record PatientExceptionDto(String message, HttpStatus status) {
}
