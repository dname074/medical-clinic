package pl.javakurs.dname074.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.javakurs.dname074.model.exception.institution.InstitutionExistsException;

import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
public class Doctor {
    private Long id;
    private String email;
    private String password;
    private User user;
    private Specialization specialization;
    private List<Institution> institutions;
    private List<Visit> visits;

    public void update(Doctor createDoctorCommand) {
        this.email = createDoctorCommand.getEmail();
        this.password = createDoctorCommand.getPassword();
        this.user.setFirstName(createDoctorCommand.getUser().getFirstName());
        this.user.setLastName(createDoctorCommand.getUser().getLastName());
        this.specialization = createDoctorCommand.getSpecialization();
    }

    public void addInstitution(Institution newInstitution) {
        boolean exists = institutions.stream()
                        .anyMatch(institution -> institution.getName().equals(newInstitution.getName()));
        if (exists) {
            throw new InstitutionExistsException("This doctor is already assigned to this institution");
        }
        institutions.add(newInstitution);
    }

    public void removeInstitution(Institution removedInstitution) {
        boolean exists = institutions.stream()
                .anyMatch(institution -> institution.getName().equals(removedInstitution.getName()));
        if (exists) {
            throw new InstitutionExistsException("This doctor is already assigned to this institution");
        }
        institutions.remove(removedInstitution);
    }

    public void addVisit(Visit newVisit) {
        visits.add(newVisit);
    }

    public void removeInstitutionAssociations() {
        for (Institution institution : institutions) {
            institution.getDoctors().remove(this);
        }
    }

    @Override
    public String toString() {
        String doctorString = "Doctor{" +
              +  "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", user_id=" + user.getId() +
                ", specialization=" + specialization;
        if (institutions != null) {
            doctorString += ", institutions_ids=" + institutions.stream()
                    .map(Institution::getId)
                    .toList()+
                    '}';
        }
        return doctorString;
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
