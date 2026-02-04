package com.dname074.medicalclinic.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class MedicalClinicException extends RuntimeException {
  private final HttpStatus status;

  public MedicalClinicException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }
}
