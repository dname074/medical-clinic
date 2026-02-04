package com.dname074.medicalclinic.dto;

import com.dname074.medicalclinic.dto.simple.SimpleDoctorDto;
import com.dname074.medicalclinic.dto.simple.SimplePatientDto;

import java.time.LocalDateTime;

public record VisitDto(Long id, LocalDateTime startDate, LocalDateTime endDate,
                       SimpleDoctorDto doctor, SimplePatientDto patient) {
}
