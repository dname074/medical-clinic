package com.dname074.medicalclinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="Patient")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="email")
    private String email;
    @Column(name="password")
    private String password;
    @Column(name="idCardNumber")
    private int idCardNo;
    @Column(name="firstName")
    private String firstName;
    @Column(name="lastName")
    private String lastName;
    @Column(name="phoneNumber")
    private String phoneNumber;
    @Column(name="birthday")
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
