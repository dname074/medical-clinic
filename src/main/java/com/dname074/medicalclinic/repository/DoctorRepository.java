package com.dname074.medicalclinic.repository;

import com.dname074.medicalclinic.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {
    Optional<Doctor> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {
            "user",
            "institutions"
    })
    Page<Doctor> findAll(@Nullable Specification<Doctor> specification, Pageable pageable);
}
