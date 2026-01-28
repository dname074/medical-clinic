package com.dname074.medicalclinic.repository;

import com.dname074.medicalclinic.model.Institution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByName(String name);

    @Query("select distinct i from Institution i left join fetch i.doctors") // dzieki left join, nawet gdy instytucja nie ma doktora to zostanie zwrócona w wyniku
    Page<Institution> findAllWithDoctors(Pageable pageable);
}
