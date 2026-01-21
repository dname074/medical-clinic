package com.dname074.medicalclinic.model;

import com.dname074.medicalclinic.dto.CreateInstitutionCommand;
import com.dname074.medicalclinic.exception.DoctorAlreadyExistsException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "institutions",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
public class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String town;
    private String zipCode;
    private String street;
    private Integer placeNo;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "institution_doctor",
            joinColumns = @JoinColumn(name = "institution_id"),
            inverseJoinColumns = @JoinColumn(name="doctor_id")
    )
    private List<Doctor> doctors;

    public void update(CreateInstitutionCommand createInstitutionCommand) {
        this.name = createInstitutionCommand.name();
        this.town = createInstitutionCommand.town();
        this.zipCode = createInstitutionCommand.zipCode();
        this.street = createInstitutionCommand.street();
        this.placeNo = createInstitutionCommand.placeNo();
    }

    public void addDoctor(Doctor newDoctor) {
        boolean exists = doctors.stream()
                .anyMatch(doctor -> doctor.getEmail().equals(newDoctor.getEmail()));

        if (exists) {
            throw new DoctorAlreadyExistsException("Podany doktor należy już do tej placówki");
        }

        doctors.add(newDoctor);
    }

    public void removeDoctor(Doctor doctor) {
        this.doctors.remove(doctor);
        doctor.getInstitutions().remove(this);
    }

    @PreRemove
    public void removeDoctorAssociations() {
        for (Doctor doctor : doctors) {
            doctor.getInstitutions().remove(this);
        }
    }
}
