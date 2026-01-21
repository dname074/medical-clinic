package com.dname074.medicalclinic.dto;

import java.time.LocalDate;

public record PatientDto(Long id, String email, int idCardNo, String firstName, String surname, String phoneNumber,
                         LocalDate birthday) {
}
