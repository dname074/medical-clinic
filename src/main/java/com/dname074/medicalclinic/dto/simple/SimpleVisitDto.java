package com.dname074.medicalclinic.dto.simple;

import com.dname074.medicalclinic.model.VisitStatus;

import java.time.LocalDateTime;

public record SimpleVisitDto(Long id, LocalDateTime startDate, LocalDateTime endDate, VisitStatus status, SimpleDoctorDto doctor) {
}
