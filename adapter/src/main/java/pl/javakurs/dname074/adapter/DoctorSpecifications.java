package pl.javakurs.dname074.adapter;

import com.dname074.medicalclinic.model.Doctor;
import com.dname074.medicalclinic.model.Specialization;
import org.springframework.data.jpa.domain.Specification;

public class DoctorSpecifications {
    public static Specification<Doctor> hasSpecialization(Specialization specialization) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("specialization"), specialization);
    }
}
