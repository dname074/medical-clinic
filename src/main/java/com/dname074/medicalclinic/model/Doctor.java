package com.dname074.medicalclinic.model;

import com.dname074.medicalclinic.dto.command.CreateDoctorCommand;
import com.dname074.medicalclinic.exception.InstitutionExistsException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "doctors", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @Enumerated(EnumType.STRING)
    private Specialization specialization;
    @ManyToMany(mappedBy = "doctors", fetch = FetchType.LAZY)
    private List<Institution> institutions;
    @OneToMany(mappedBy = "doctor")
    private List<Visit> visit;

    public void update(CreateDoctorCommand createDoctorCommand) {
        this.email = createDoctorCommand.email();
        this.password = createDoctorCommand.password();
        this.user.setFirstName(createDoctorCommand.firstName());
        this.user.setLastName(createDoctorCommand.lastName());
        this.specialization = createDoctorCommand.specialization();
    }

    public void addInstitution(Institution newInstitution) {
        boolean exists = institutions.stream()
                        .anyMatch(institution -> institution.getName().equals(newInstitution.getName()));
        if (exists) {
            throw new InstitutionExistsException("Ten doktor jest już przypisany do podanej placówki");
        }
        institutions.add(newInstitution);
    }

    @PreRemove
    public void removeInstitutionAssociations() {
        for (Institution institution : institutions) {
            institution.getDoctors().remove(this);
        }
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", user_id=" + user.getId() +
                ", specialization=" + specialization +
                ", institutions_ids=" + institutions.stream()
                .map(Institution::getId)
                .toList()+
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this==o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return id != null && Objects.equals(id, doctor.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
