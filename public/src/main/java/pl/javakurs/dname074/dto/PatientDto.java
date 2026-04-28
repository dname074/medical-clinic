package com.dname074.medicalclinic.dto;

import pl.javakurs.dname074.dto.simple.SimpleVisitDto;

import java.time.LocalDate;
import java.util.List;

public record PatientDto(Long id, String email, String idCardNo, String phoneNumber,
                         LocalDate birthday, UserDto user, List<SimpleVisitDto> visits) {
}
