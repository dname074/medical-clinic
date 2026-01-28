package com.dname074.medicalclinic.model;

import com.dname074.medicalclinic.dto.command.CreatePatientCommand;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
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
    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
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

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", idCardNo=" + idCardNo +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", birthday=" + birthday +
                ", user_id=" + user.getId() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return id != null && Objects.equals(id, patient.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
