package com.dname074.medicalclinic.model;

import java.time.LocalDate;

public record PatientDto(String email, int idCardNo, String firstName, String surname, String phoneNumber,
                         LocalDate birthday) {
}
