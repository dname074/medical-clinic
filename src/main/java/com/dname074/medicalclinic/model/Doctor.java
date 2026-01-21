package com.dname074.medicalclinic.model;

import com.dname074.medicalclinic.dto.CreateDoctorCommand;
import com.dname074.medicalclinic.exception.InstitutionExistsException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
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
    @ManyToMany(mappedBy = "doctors")
    private List<Institution> institutions;

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
}
