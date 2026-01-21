package com.dname074.medicalclinic.dto;

import com.dname074.medicalclinic.model.Specialization;

public record DoctorDto(Long id, String email, String firstName,
                        String lastName, Specialization specialization) {
}
