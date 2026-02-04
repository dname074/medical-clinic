package com.dname074.medicalclinic.dto;

import org.springframework.http.HttpStatus;

public record MedicalClinicExceptionDto(String message, HttpStatus status) {
}
