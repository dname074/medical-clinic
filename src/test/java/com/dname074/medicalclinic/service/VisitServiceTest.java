package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.dto.VisitDto;
import com.dname074.medicalclinic.dto.command.CreateDoctorCommand;
import com.dname074.medicalclinic.dto.command.CreatePatientCommand;
import com.dname074.medicalclinic.dto.command.CreateVisitCommand;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.mapper.VisitMapper;
import com.dname074.medicalclinic.model.Doctor;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.model.Specialization;
import com.dname074.medicalclinic.model.Visit;
import com.dname074.medicalclinic.repository.DoctorRepository;
import com.dname074.medicalclinic.repository.PatientRepository;
import com.dname074.medicalclinic.repository.VisitRepository;
import com.dname074.medicalclinic.validation.VisitValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class VisitServiceTest {
    VisitRepository visitRepository;
    DoctorRepository doctorRepository;
    PatientRepository patientRepository;
    VisitMapper visitMapper;
    VisitValidator validator;
    VisitService visitService;
    PageMapper pageMapper;

    @BeforeEach
    void setup() {
        this.visitRepository = Mockito.mock(VisitRepository.class);
        this.doctorRepository = Mockito.mock(DoctorRepository.class);
        this.patientRepository = Mockito.mock(PatientRepository.class);
        this.visitMapper = Mappers.getMapper(VisitMapper.class);
        this.validator = Mockito.mock(VisitValidator.class);
        this.pageMapper = Mappers.getMapper(PageMapper.class);
        this.visitService = new VisitService(visitRepository, doctorRepository, patientRepository, visitMapper, validator, pageMapper);
    }

//    @Test
//    void getVisitsByPatientId_VisitFound_PageReturned() {
//        // given
//        Doctor doctor = createDoctor();
//        Visit visit = createVisit();
//        Patient patient = createPatient();
//        visit.setDoctor(doctor);
//        visit.setPatient(patient);
//        List<Visit> visits = List.of(visit);
//        Pageable pageable = PageRequest.of(0, 1);
//        Page page = new PageImpl(visits, pageable, 1);
//        when(visitRepository.findByPatientId(patient.getId(), pageable));
//        // when
//        Page<VisitDto> result = visitService.getVisitsByPatientId(patient.getId(), pageable);
//        // then
//        Assertions.assertAll(
//                () -> assertEquals(page.getTotalPages(), result.getTotalPages()),
//                () -> assertEquals(page.getTotalElements(), result.getTotalElements()),
//                () -> assertFalse(result.getContent().isEmpty())
//        );
//        verify(visitRepository, times(1)).findByPatientId(1L, pageable);
//        verifyNoMoreInteractions(visitRepository);
//        verifyNoInteractions(doctorRepository, patientRepository);
//    }

//    @Test
//    void addAvailableVisit_DoctorFoundAndVisitDateCorrect_VisitReturned() {
//        // given
//        CreateVisitCommand createVisitCommand = makeCreateVisitCommand();
//        Doctor doctor = createDoctor();
//        Visit visit = visitMapper.toEntity(createVisitCommand);
//        Doctor updatedDoctor = createDoctor();
//        Visit updatedVisit = createVisit();
//        updatedDoctor.addVisit(updatedVisit);
//        updatedVisit.setDoctor(doctor);
//        when(doctorRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
//        when(visitRepository.existsByStartDateLessThanAndEndDateGreaterThan(visit.getEndDate(), visit.getStartDate())).thenReturn(false);
//        when(visitRepository.save(any())).thenReturn(updatedVisit);
//        // when
//        VisitDto result = visitService.addAvailableVisit(createVisitCommand);
//        // then
//        Assertions.assertAll(
//                () -> assertEquals(result.doctor(), doctor)
//        );
//    }

    private CreateVisitCommand makeCreateVisitCommand() {
        return new CreateVisitCommand(1L,
                LocalDateTime.of(2026, 2, 20, 18, 0, 0),
                LocalDateTime.of(2026, 2, 20, 19, 0, 0));
    }

    private Visit createVisit() {
        return visitMapper.toEntity(makeCreateVisitCommand());
    }

//    private CreatePatientCommand makeCreatePatientCommand() {
//        return new CreatePatientCommand("email", "123", "55", "Jan", "Kowalski",
//                "555555555", LocalDate.of(2007, 11, 25));
//    }
//
//    private Patient createPatient() {
//        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
//        return patientMapper.toEntity(createPatientCommand);
//    }
//
//    private CreateDoctorCommand makeCreateDoctorCommand() {
//        return new CreateDoctorCommand("email", "Jan", "Kowalski",
//                "123", Specialization.DERMATOLOGIST);
//    }
//
//    private Doctor createDoctor() {
//        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
//        return doctorMapper.toEntity(createDoctorCommand);
//    }
}
