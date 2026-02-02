package com.dname074.medicalclinic.dto.simple;

import java.time.LocalDateTime;

public record SimpleVisitDto(Long id, LocalDateTime startDateTime, LocalDateTime endDateTime, SimpleDoctorDto doctor) {
}
