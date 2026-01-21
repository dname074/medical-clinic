package com.dname074.medicalclinic.model;

import com.dname074.medicalclinic.dto.CreatePatientCommand;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name="patients",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
        })
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
    @Column(name="phoneNumber")
    private String phoneNumber;
    @Column(name="birthday")
    private LocalDate birthday;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="user_id", referencedColumnName = "id")
    private User user;

    public void update(CreatePatientCommand createPatientCommand) {
        setEmail(createPatientCommand.email());
        setPassword(createPatientCommand.password());
        setIdCardNo(createPatientCommand.idCardNo());
        setPhoneNumber(createPatientCommand.phoneNumber());
        setBirthday(createPatientCommand.birthday());
        user.setFirstName(createPatientCommand.firstName());
        user.setLastName(createPatientCommand.lastName());
    }
}
