package com.dname074.medicalclinic.dto;

import pl.javakurs.dname074.dto.simple.SimpleInstitutionDto;
import com.dname074.medicalclinic.model.Specialization;

import java.util.List;

public record DoctorDto(Long id, String email, Specialization specialization,
                        UserDto user, List<SimpleInstitutionDto> institutions) {
}
