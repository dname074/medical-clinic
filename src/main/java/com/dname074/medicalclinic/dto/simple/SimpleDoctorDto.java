package com.dname074.medicalclinic.dto.simple;

import com.dname074.medicalclinic.dto.UserDto;
import com.dname074.medicalclinic.model.Specialization;

public record SimpleDoctorDto(Long id, String email, Specialization specialization, UserDto user) {
}
