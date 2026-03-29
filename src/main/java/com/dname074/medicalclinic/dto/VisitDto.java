package com.dname074.medicalclinic.dto;

import com.dname074.medicalclinic.dto.simple.SimpleDoctorDto;
import com.dname074.medicalclinic.dto.simple.SimplePatientDto;
import com.dname074.medicalclinic.model.VisitStatus;

import java.time.LocalDateTime;

public record VisitDto(Long id, LocalDateTime startDate, LocalDateTime endDate, VisitStatus status,
                       SimpleDoctorDto doctor, SimplePatientDto patient) {
}
