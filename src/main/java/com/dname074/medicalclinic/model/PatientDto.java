package com.dname074.medicalclinic.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@RequiredArgsConstructor
@Getter
public class PatientDto {
    private final String email;
    private final int idCardNo;
    private final String firstName;
    private final String surname;
    private final String phoneNumber;
    private final LocalDate birthday;
}
