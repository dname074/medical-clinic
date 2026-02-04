package com.dname074.medicalclinic.repository;

import com.dname074.medicalclinic.model.Visit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    boolean existsByStartDateLessThanAndEndDateGreaterThan(LocalDateTime endDate, LocalDateTime startDate);
    Page<Visit> findByPatientId(Long patientId, Pageable pageable);
}
