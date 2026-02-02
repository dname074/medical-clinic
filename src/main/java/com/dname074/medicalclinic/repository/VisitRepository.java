package com.dname074.medicalclinic.repository;

import com.dname074.medicalclinic.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    Optional<Visit> findByStartDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    Optional<Visit> findByEndDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
