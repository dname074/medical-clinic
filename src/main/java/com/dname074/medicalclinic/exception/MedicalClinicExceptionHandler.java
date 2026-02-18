package com.dname074.medicalclinic.exception;

import com.dname074.medicalclinic.dto.MedicalClinicExceptionDto;
import com.dname074.medicalclinic.dto.ValidationExceptionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class MedicalClinicExceptionHandler {
    @ExceptionHandler(MedicalClinicException.class)
    public ResponseEntity<MedicalClinicExceptionDto> handleMedicalClinicException(MedicalClinicException exception) {
        exceptionLog(exception.getMessage());
        HttpStatus httpStatus = exception.getStatus();
        return ResponseEntity.status(httpStatus).body(new MedicalClinicExceptionDto(exception.getMessage(), httpStatus));
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<MedicalClinicExceptionDto> handleDateTimeParseException(DateTimeParseException exception) {
        exceptionLog(exception.getMessage());
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(new MedicalClinicExceptionDto(exception.getMessage(), httpStatus));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationExceptionDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        exceptionLog(exception.getMessage());
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        List<String> messages = new ArrayList<>();
        exception.getBindingResult().getAllErrors().forEach(error -> messages.add(((FieldError) error).getField() + " - " + error.getDefaultMessage()));
        return ResponseEntity.status(httpStatus).body(new ValidationExceptionDto(httpStatus, messages));
    }

    private void exceptionLog(String message) {
        log.error("Exception log: {}", message);
    }
}
