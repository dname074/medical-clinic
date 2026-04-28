package pl.javakurs.dname074.dto.exception;

import org.springframework.http.HttpStatus;

public record MedicalClinicExceptionDto(String message, HttpStatus status) {
}
