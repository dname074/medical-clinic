package com.dname074.medicalclinic.repository;

import com.dname074.medicalclinic.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByEmail(String email);
    @Query("select distinct d from Doctor d left join fetch d.institutions join fetch d.user")
    Page<Doctor> findAllWithUsers(Pageable pageable);
}
