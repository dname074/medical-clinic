package com.dname074.medicalclinic.dto;

import pl.javakurs.dname074.dto.simple.SimpleDoctorDto;
import pl.javakurs.dname074.dto.simple.SimplePatientDto;
import com.dname074.medicalclinic.model.VisitStatus;

import java.time.LocalDateTime;

public record VisitDto(Long id, LocalDateTime startDate, LocalDateTime endDate, VisitStatus visitStatus,
                       SimpleDoctorDto doctor, SimplePatientDto patient) {
}
