package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.authorization.AuthorizationService;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.VisitDto;
import com.dname074.medicalclinic.dto.command.CreateVisitCommand;
import com.dname074.medicalclinic.exception.doctor.DoctorNotFoundException;
import com.dname074.medicalclinic.exception.patient.PatientNotFoundException;
import com.dname074.medicalclinic.exception.visit.VisitAlreadyCanceledException;
import com.dname074.medicalclinic.exception.visit.VisitAlreadyTakenException;
import com.dname074.medicalclinic.exception.visit.VisitExpiredException;
import com.dname074.medicalclinic.exception.visit.VisitNotFoundException;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.mapper.VisitMapper;
import com.dname074.medicalclinic.model.Doctor;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.model.Role;
import com.dname074.medicalclinic.model.Specialization;
import com.dname074.medicalclinic.model.Visit;
import com.dname074.medicalclinic.model.VisitStatus;
import com.dname074.medicalclinic.repository.DoctorRepository;
import com.dname074.medicalclinic.repository.PatientRepository;
import com.dname074.medicalclinic.repository.VisitRepository;
import com.dname074.medicalclinic.specification.VisitSpecifications;
import com.dname074.medicalclinic.validation.VisitValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final VisitMapper visitMapper;
    private final VisitValidator validator;
    private final PageMapper pageMapper;
    private final Clock clock;
    private final AuthorizationService authService;

    public PageDto<VisitDto> getVisitsByPatientId(Long id, Pageable pageRequest) {
        log.info("Process of finding patient's visits started");
        Specification<Visit> filters = VisitSpecifications.hasPatientId(id);
        PageDto<VisitDto> page = pageMapper.toVisitDto(visitRepository.findAll(filters, pageRequest)
                .map(visitMapper::toDto));
        log.info("Process of finding patient's visits ended");
        return page;
    }

    public PageDto<VisitDto> getVisitsByDoctorId(Long id, VisitStatus status, Pageable pageRequest, Authentication auth) {
        log.info("Process of finding doctor's visits started");
        Doctor doctor = findDoctorById(id);

        Jwt jwt = authService.extractJwt(auth);
        boolean isOwner = isDoctorOwner(doctor, jwt);
        validateVisitsViewAccess(auth, status, isOwner);

        PageDto<VisitDto> visitsPage = pageMapper.toVisitDto(filterDoctorVisits(id, status, pageRequest)
                .map(visitMapper::toDto));
        log.info("Process of finding doctor's visits ended");
        return visitsPage;
    }

    public PageDto<VisitDto> getFilteredVisits(LocalDate fromDate, LocalDate toDate, Specialization specialization,
                                               VisitStatus status, Pageable pageRequest, Authentication auth) {
        log.info("Process of finding visits by date and specialization started");
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.plusDays(1).atStartOfDay();
        validateVisitsViewAccess(auth, status, false);
        PageDto<VisitDto> page = pageMapper.toVisitDto(filterVisits(from, to, specialization, status, pageRequest)
                .map(visitMapper::toDto));
        log.info("Process of finding visits by date and specialization ended");
        return page;
    }

    @Transactional
    public VisitDto addAvailableVisit(CreateVisitCommand createVisitCommand, Authentication auth) {
        log.info("Process of creating new visit started");
        Doctor doctor = findDoctorById(createVisitCommand.doctorId());
        Jwt jwt = authService.extractJwt(auth);
        if (!authService.hasRole(auth, Role.ADMIN) && !isDoctorOwner(doctor, jwt)) {
            throw new AccessDeniedException("Doctor can't create visits for other doctors");
        }
        validator.validateVisitDate(createVisitCommand.startDate(), createVisitCommand.endDate());

        Visit visit = visitMapper.toEntity(createVisitCommand);
        visit.setDoctor(doctor);
        doctor.addVisit(visit);
        visit.setVisitStatus(VisitStatus.AVAILABLE);
        log.info("Process of creating new visit ended");
        return visitMapper.toDto(visitRepository.save(visit));
    }

    @Transactional
    public VisitDto assign(Long visitId, Long patientId, Authentication auth) {
        log.info("Process of assigning patient to visit started");
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("Nie znaleziono terminu wizyty o podanym id"));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Nie znaleziono pacjenta o podanym id"));
        validateVisitAssignment(auth, patient, visit);

        visit.setPatient(patient);
        patient.addVisit(visit);
        visit.setVisitStatus(VisitStatus.BOOKED);
        visitRepository.save(visit);
        log.info("Process of assigning patient to visit ended");
        return visitMapper.toDto(visit);
    }

    @Transactional
    public VisitDto cancelVisit(Long visitId, Authentication auth) {
        log.info("Process of cancelling visit started");
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("Visit with provided id does not exist"));
        validateVisitCancellation(auth, visit);

        visit.setVisitStatus(VisitStatus.CANCELED);
        visitRepository.save(visit);
        log.info("Process of cancelling visit ended");
        return visitMapper.toDto(visit);
    }

    private void validateVisitsViewAccess(Authentication auth, VisitStatus status, boolean isOwner) {
        if (authService.hasRole(auth, Role.ADMIN)) {
            return;
        }
        if (authService.hasRole(auth, Role.PATIENT)) {
            if (status != VisitStatus.AVAILABLE) {
                throw new AccessDeniedException("Patients can only view available doctor's visits");
            }
            return;
        }
        if (authService.hasRole(auth, Role.DOCTOR)) {
            if (status == VisitStatus.AVAILABLE || isOwner) {
                return;
            }
            throw new AccessDeniedException("Doctor can only view own visits");
        }
        throw new AccessDeniedException("Unknown role");
    }

    private void validateVisitAssignment(Authentication auth, Patient patient, Visit visit) {
        if (!authService.hasRole(auth, Role.ADMIN) && !isPatientOwner(patient, authService.extractJwt(auth))) {
            throw new AccessDeniedException("Patient can only book visits for himself/herself");
        }
        if (visit.getPatient() != null || visit.getVisitStatus() != VisitStatus.AVAILABLE) {
            throw new VisitAlreadyTakenException("Ten termin wizyty jest już zajęty");
        }
        if (visit.getStartDate().isBefore(LocalDateTime.now(clock))) {
            throw new VisitExpiredException("Ten termin wizyty poprzedza aktualną datę i nie jest już dostępny");
        }
    }

    private void validateVisitCancellation(Authentication auth, Visit visit) {
        if (!authService.hasRole(auth, Role.ADMIN) && !isDoctorOwner(visit.getDoctor(), authService.extractJwt(auth))) {
            throw new AccessDeniedException("Only doctor can cancel his visits");
        }
        if (visit.getVisitStatus() == VisitStatus.CANCELED) {
            throw new VisitAlreadyCanceledException("This visit has already been canceled before");
        }
    }

    private Page<Visit> filterDoctorVisits(Long id, VisitStatus status, Pageable pageRequest) {
        Specification<Visit> filters = VisitSpecifications.hasDoctorId(id);
        if (status != null) {
            filters = filters.and(VisitSpecifications.hasStatus(status));
        }
        return visitRepository.findAll(filters, pageRequest);
    }

    private Page<Visit> filterVisits(LocalDateTime from, LocalDateTime to, Specialization specialization, VisitStatus status, Pageable pageRequest) {
        Specification<Visit> filters =  VisitSpecifications.hasDateBetween(from, to);
        if (status != null) {
            filters = filters.and(VisitSpecifications.hasStatus(status));
        }
        if (specialization != null) {
            filters = filters.and(VisitSpecifications.hasSpecialization(specialization));
        }
        return visitRepository.findAll(filters, pageRequest);
    }

    private boolean isDoctorOwner(Doctor doctor, Jwt jwt) {
        return doctor.getUser().getKeycloakId().equals(jwt.getClaimAsString("sub"));
    }

    private boolean isPatientOwner(Patient patient, Jwt jwt) {
        return patient.getUser().getKeycloakId().equals(jwt.getClaimAsString("sub"));
    }

    private Doctor findDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
    }
}
