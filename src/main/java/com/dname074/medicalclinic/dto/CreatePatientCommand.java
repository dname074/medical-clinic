package com.dname074.medicalclinic.dto;

import java.time.LocalDate;

public record CreatePatientCommand(String email,
        String password,
        int idCardNo,
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate birthday) {

}
