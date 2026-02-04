package com.dname074.medicalclinic.dto;

import com.dname074.medicalclinic.dto.simple.SimpleVisitDto;

import java.time.LocalDate;
import java.util.List;

public record PatientDto(Long id, String email, String idCardNo, String phoneNumber,
                         LocalDate birthday, UserDto user, List<SimpleVisitDto> visits) {
}
