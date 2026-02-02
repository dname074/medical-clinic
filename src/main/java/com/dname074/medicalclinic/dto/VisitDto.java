package com.dname074.medicalclinic.dto;

import com.dname074.medicalclinic.dto.simple.SimpleDoctorDto;
import com.dname074.medicalclinic.dto.simple.SimplePatientDto;

import java.time.LocalDateTime;

public record VisitDto(Long id, LocalDateTime startDateTime, LocalDateTime endDateTime,
                       SimpleDoctorDto doctor, SimplePatientDto patient) {
}
