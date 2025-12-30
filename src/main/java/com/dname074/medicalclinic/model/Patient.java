package com.dname074.medicalclinic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Patient {
    private String email;
    private String password;
    private int idCardNo;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate birthday;

    public void update(Patient patient) {
        setEmail(patient.getEmail());
        setPassword(patient.getPassword());
        setIdCardNo(patient.getIdCardNo());
        setFirstName(patient.getFirstName());
        setLastName(patient.getLastName());
        setPhoneNumber(patient.getPhoneNumber());
        setBirthday(patient.getBirthday());
    }
}
