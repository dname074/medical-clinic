package com.dname074.medicalclinic.service;

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

    public PageDto<VisitDto> getVisitsByPatientId(Long id, Pageable pageRequest) {
        log.info("Process of finding patient's visits started");
        Specification<Visit> filters = VisitSpecifications.hasPatientId(id);
        PageDto<VisitDto> page = pageMapper.toVisitDto(visitRepository.findAll(filters, pageRequest)
                .map(visitMapper::toDto));
        log.info("Process of finding patient's visits ended");
        return page;
    }

    public PageDto<VisitDto> getVisitsByDoctorId(Long id, VisitStatus status, Pageable pageRequest) {
        log.info("Process of finding doctor's visits started");
        Specification<Visit> filters = VisitSpecifications.hasDoctorId(id);
        if (status != null) {
            filters = filters.and(VisitSpecifications.hasStatus(status));
        }
        PageDto<VisitDto> visitsPage = pageMapper.toVisitDto(visitRepository.findAll(filters, pageRequest)
                .map(visitMapper::toDto));
        log.info("Process of finding doctor's visits ended");
        return visitsPage;
    }

    public PageDto<VisitDto> getFilteredVisits(LocalDate fromDate, LocalDate toDate, Specialization specialization, VisitStatus status, Pageable pageRequest) {
        log.info("Process of finding visits by date and specialization started");
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.plusDays(1).atStartOfDay();
        PageDto<VisitDto> page = pageMapper.toVisitDto(filterVisits(from, to, specialization, status, pageRequest)
                .map(visitMapper::toDto));
        log.info("Process of finding visits by date and specialization ended");
        return page;
    }

    @Transactional
    public VisitDto addAvailableVisit(CreateVisitCommand createVisitCommand) {
        log.info("Process of creating new visit started");
        validator.validateVisitDate(createVisitCommand.startDate(), createVisitCommand.endDate());
        Doctor doctor = doctorRepository.findById(createVisitCommand.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Nie znaleziono doktora o podanym id"));
        Visit visit = visitMapper.toEntity(createVisitCommand);
        visit.setDoctor(doctor);
        doctor.addVisit(visit);
        visit.setVisitStatus(VisitStatus.AVAILABLE);
        log.info("Process of creating new visit ended");
        return visitMapper.toDto(visitRepository.save(visit));
    }

    @Transactional
    public VisitDto assign(Long visitId, Long patientId) {
        log.info("Process of assigning patient to visit started");
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("Nie znaleziono terminu wizyty o podanym id"));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Nie znaleziono pacjenta o podanym id"));
        if (visit.getPatient() != null) {
            throw new VisitAlreadyTakenException("Ten termin wizyty jest już zajęty");
        }
        if (visit.getStartDate().isBefore(LocalDateTime.now(clock))) {
            throw new VisitExpiredException("Ten termin wizyty poprzedza aktualną datę i nie jest już dostępny");
        }
        visit.setPatient(patient);
        patient.addVisit(visit);
        visit.setVisitStatus(VisitStatus.BOOKED);
        visitRepository.save(visit);
        log.info("Process of assigning patient to visit ended");
        return visitMapper.toDto(visit);
    }

    @Transactional
    public VisitDto cancelVisit(Long visitId) {
        log.info("Process of cancelling visit started");
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("Visit with provided id does not exist"));
        if (visit.getVisitStatus() == VisitStatus.CANCELED) {
            throw new VisitAlreadyCanceledException("This visit has already been canceled before");
        }
        visit.setVisitStatus(VisitStatus.CANCELED);
        visitRepository.save(visit);
        log.info("Process of cancelling visit ended");
        return visitMapper.toDto(visit);
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
}
