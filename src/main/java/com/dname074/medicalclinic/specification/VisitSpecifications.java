package com.dname074.medicalclinic.specification;

import com.dname074.medicalclinic.model.Specialization;
import com.dname074.medicalclinic.model.Visit;
import com.dname074.medicalclinic.model.VisitStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class VisitSpecifications {
    public static Specification<Visit> hasDateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), from),
                        criteriaBuilder.lessThan(root.get("startDate"), to)
                );
    }

    public static Specification<Visit> hasSpecialization(Specialization specialization) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("doctor").get("specialization"), specialization);
    }

    public static Specification<Visit> hasStatus(VisitStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("visitStatus"), status);
    }

    public static Specification<Visit> hasDoctorId(Long id) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("doctor").get("id"), id);
    }

    public static Specification<Visit> hasPatientId(Long id) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("patient").get("id"), id);
    }
}
