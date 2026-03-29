package com.dname074.medicalclinic.repository;

import com.dname074.medicalclinic.model.Specialization;
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
    Page<Visit> findByDoctorId(Long doctorId, Pageable pageable);
    Page<Visit> findByDoctorIdAndPatientIsNull(Long doctorId, Pageable pageable);
    Page<Visit> findByStartDateGreaterThanEqualAndStartDateLessThanAndDoctorSpecializationAndPatientIsNull(
            LocalDateTime startDay, LocalDateTime endDay, Specialization specialization, Pageable pageable);
    Page<Visit> findByStartDateGreaterThanEqualAndStartDateLessThanAndDoctorSpecialization(
            LocalDateTime startDay, LocalDateTime endDay, Specialization specialization, Pageable pageable);
    Page<Visit> findByStartDateGreaterThanEqualAndStartDateLessThanAndPatientIsNull(
            LocalDateTime startDay, LocalDateTime endDay, Pageable pageable);
    Page<Visit> findByStartDateGreaterThanEqualAndStartDateLessThan(
            LocalDateTime startDay, LocalDateTime endDay, Pageable pageable);
}
