package pl.javakurs.dname074.adapter.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.javakurs.dname074.dto.command.CreatePatientCommand;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "patients",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        })
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    @Column(name = "id_card_number")
    private String idCardNo;
    @Column(name = "phone_number")
    private String phoneNumber;
    private LocalDate birthday;
    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @OneToMany(mappedBy = "patient")
    private List<Visit> visits;

    public void update(CreatePatientCommand createPatientCommand) {
        setEmail(createPatientCommand.email());
        setPassword(createPatientCommand.password());
        setIdCardNo(createPatientCommand.idCardNo());
        setPhoneNumber(createPatientCommand.phoneNumber());
        setBirthday(createPatientCommand.birthday());
        if (user != null) {
            user.setFirstName(createPatientCommand.firstName());
            user.setLastName(createPatientCommand.lastName());
        }
    }

    public void addVisit(Visit newVisit) {
        visits.add(newVisit);
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
}
