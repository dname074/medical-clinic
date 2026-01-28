package com.dname074.medicalclinic.repository;

import com.dname074.medicalclinic.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmail(String email);

    @Query("select distinct p from Patient p join fetch p.user")
    Page<Patient> findAllWithUsers(Pageable pageable);
}
