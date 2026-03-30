package com.dname074.medicalclinic.repository;

import com.dname074.medicalclinic.model.Specialization;
import com.dname074.medicalclinic.model.Visit;
import com.dname074.medicalclinic.model.VisitStatus;
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
    Page<Visit> findByStartDateGreaterThanEqualAndStartDateLessThanAndDoctorSpecializationAndVisitStatusAndPatientIsNull(
            LocalDateTime startDay, LocalDateTime endDay, Specialization specialization, VisitStatus status, Pageable pageable);
    Page<Visit> findByStartDateGreaterThanEqualAndStartDateLessThanAndDoctorSpecialization(
            LocalDateTime startDay, LocalDateTime endDay, Specialization specialization, Pageable pageable);
    Page<Visit> findByStartDateGreaterThanEqualAndStartDateLessThanAndVisitStatusAndPatientIsNull(
            LocalDateTime startDay, LocalDateTime endDay, VisitStatus status, Pageable pageable);
    Page<Visit> findByStartDateGreaterThanEqualAndStartDateLessThan(
            LocalDateTime startDay, LocalDateTime endDay, Pageable pageable);
}
