package com.dname074.medicalclinic.dto.simple;

import com.dname074.medicalclinic.dto.UserDto;

import java.time.LocalDate;
import java.util.List;

public record SimplePatientDto(Long id, String email, String idCardNo, String phoneNumber,
                              LocalDate birthday, UserDto user, List<SimpleVisitDto> visits) {
}
