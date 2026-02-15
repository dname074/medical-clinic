package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.argumentmatcher.VisitArgumentMatcher;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.VisitDto;
import com.dname074.medicalclinic.dto.command.CreateDoctorCommand;
import com.dname074.medicalclinic.dto.command.CreatePatientCommand;
import com.dname074.medicalclinic.dto.command.CreateVisitCommand;
import com.dname074.medicalclinic.exception.doctor.DoctorNotFoundException;
import com.dname074.medicalclinic.exception.patient.PatientNotFoundException;
import com.dname074.medicalclinic.exception.visit.InvalidVisitException;
import com.dname074.medicalclinic.exception.visit.VisitAlreadyTakenException;
import com.dname074.medicalclinic.exception.visit.VisitExpiredException;
import com.dname074.medicalclinic.exception.visit.VisitNotFoundException;
import com.dname074.medicalclinic.mapper.DoctorMapper;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.mapper.PatientMapper;
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

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class VisitServiceTest {
    VisitRepository visitRepository;
    DoctorRepository doctorRepository;
    PatientRepository patientRepository;
    VisitMapper visitMapper;
    VisitValidator validator;
    VisitService visitService;
    PageMapper pageMapper;
    PatientMapper patientMapper;
    DoctorMapper doctorMapper;
    Clock currentDate = Clock.fixed(
            LocalDateTime.of(2026, 2, 15, 12, 0, 0)
                    .atZone(ZoneId.systemDefault())
                    .toInstant(),
            ZoneId.systemDefault()
    );

    @BeforeEach
    void setup() {
        this.visitRepository = Mockito.mock(VisitRepository.class);
        this.doctorRepository = Mockito.mock(DoctorRepository.class);
        this.patientRepository = Mockito.mock(PatientRepository.class);
        this.visitMapper = Mappers.getMapper(VisitMapper.class);
        this.validator = Mockito.mock(VisitValidator.class);
        this.pageMapper = Mappers.getMapper(PageMapper.class);
        this.patientMapper = Mappers.getMapper(PatientMapper.class);
        this.doctorMapper = Mappers.getMapper(DoctorMapper.class);
        this.visitService = new VisitService(visitRepository, doctorRepository, patientRepository,
                visitMapper, validator, pageMapper, currentDate);
    }

    @Test
    void getVisitsByPatientId_VisitFound_PageReturned() {
        // given
        Long patientId = 1L;
        Doctor doctor = createDoctor();
        Visit visit = createVisit();
        Patient patient = createPatient();
        patient.setId(patientId);
        visit.setDoctor(doctor);
        visit.setPatient(patient);
        List<Visit> visits = List.of(visit);
        Pageable pageable = PageRequest.of(0, 1);
        Page<Visit> page = new PageImpl<>(visits, pageable, 1);
        when(visitRepository.findByPatientId(patient.getId(), pageable)).thenReturn(page);
        // when
        PageDto<VisitDto> result = visitService.getVisitsByPatientId(patient.getId(), pageable);
        // then
        Assertions.assertAll(
                () -> assertEquals(page.getTotalPages(), result.totalPages()),
                () -> assertEquals(page.getTotalElements(), result.totalElements()),
                () -> assertFalse(result.content().isEmpty())
        );
        verify(visitRepository, times(1)).findByPatientId(1L, pageable);
        verifyNoMoreInteractions(visitRepository);
        verifyNoInteractions(doctorRepository, patientRepository);
    }

    @Test
    void addAvailableVisit_DoctorFoundAndVisitDateCorrect_VisitReturned() {
        // given
        Long doctorId = 1L;
        CreateVisitCommand createVisitCommand = makeCreateVisitCommand();
        Doctor doctor = createDoctor();
        doctor.setVisits(new ArrayList<>());
        Doctor updatedDoctor = createDoctor();
        Visit updatedVisit = createVisit();
        updatedDoctor.setVisits(List.of(updatedVisit));
        updatedVisit.setDoctor(doctor);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        doNothing().when(validator).validateVisitDate(updatedVisit.getStartDate(), updatedVisit.getEndDate());
        when(visitRepository.save(any())).thenReturn(updatedVisit);
        // when
        VisitDto result = visitService.addAvailableVisit(createVisitCommand);
        // then
        Assertions.assertAll(
                () -> assertEquals("email", result.doctor().email()),
                () -> assertEquals("Jan", result.doctor().user().firstName()),
                () -> assertEquals("Kowalski", result.doctor().user().lastName()),
                () -> assertEquals(Specialization.DERMATOLOGIST, result.doctor().specialization()),
                () -> assertEquals(LocalDateTime.of(2026, 3, 1, 20, 0, 0), result.startDate()),
                () -> assertEquals(LocalDateTime.of(2026, 3, 1, 21, 0, 0), result.endDate()),
                () -> assertEquals(1L, result.doctor().id())
        );
        verify(doctorRepository, times(1)).findById(1L);
        verify(validator, times(1)).validateVisitDate(updatedVisit.getStartDate(), updatedVisit.getEndDate());
        verify(visitRepository, times(1)).save(argThat(new VisitArgumentMatcher(updatedVisit)));
        verifyNoMoreInteractions(visitRepository, doctorRepository, validator);
        verifyNoInteractions(patientRepository);
    }

    @Test
    void addAvailableVisit_DateIncorrect_InvalidVisitExceptionThrown() {
        // given
        CreateVisitCommand createVisitCommand = makeCreateVisitCommand();
        doThrow(InvalidVisitException.class).when(validator).validateVisitDate(any(), any());
        // when & then
        assertThrows(InvalidVisitException.class, () -> visitService.addAvailableVisit(createVisitCommand));
        verify(validator, times(1)).validateVisitDate(createVisitCommand.startDate(), createVisitCommand.endDate());
        verifyNoMoreInteractions(validator);
        verifyNoInteractions(patientRepository, doctorRepository, visitRepository);
    }

    @Test
    void addAvailableVisit_DoctorNotFound_DoctorNotFoundExceptionThrown() {
        // given
        CreateVisitCommand createVisitCommand = makeCreateVisitCommand();
        doNothing().when(validator).validateVisitDate(createVisitCommand.startDate(), createVisitCommand.endDate());
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());
        // when & then
        DoctorNotFoundException exception = assertThrows(DoctorNotFoundException.class, () -> visitService.addAvailableVisit(createVisitCommand));
        assertEquals("Nie znaleziono doktora o podanym id", exception.getMessage());
        verify(validator, times(1)).validateVisitDate(createVisitCommand.startDate(), createVisitCommand.endDate());
        verify(doctorRepository, times(1)).findById(createVisitCommand.doctorId());
        verifyNoMoreInteractions(validator, doctorRepository);
        verifyNoInteractions(patientRepository, visitRepository);
    }

    @Test
    void assign_VisitFoundPatientFoundAndDateNotExpired_VisitAssignedAndVisitReturned() {
        // given
        Long visitId = 1L;
        Long patientId = 1L;
        Visit visit = createVisit();
        Visit updatedVisit = createVisit();
        Patient patient = createPatient();
        patient.setVisits(new ArrayList<>());
        Patient updatedPatient = createPatient();
        updatedPatient.setVisits(new ArrayList<>());
        updatedVisit.setPatient(updatedPatient);
        updatedPatient.addVisit(updatedVisit);
        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(visitRepository.save(visit)).thenReturn(visit);
        // when
        VisitDto result = visitService.assign(visitId, patientId);
        // then
        Assertions.assertAll(
                () -> assertEquals(updatedVisit.getStartDate(), result.startDate()),
                () -> assertEquals(updatedVisit.getEndDate(), result.endDate())
        );
        verify(visitRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).findById(1L);
        verify(visitRepository, times(1)).save(argThat(new VisitArgumentMatcher(updatedVisit)));
        verifyNoMoreInteractions(visitRepository, patientRepository);
        verifyNoInteractions(doctorRepository);
    }

    @Test
    void assign_VisitNotFound_VisitNotFoundExceptionThrown() {
        // given
        Long visitId = 1L;
        Long patientId = 1L;
        when(visitRepository.findById(anyLong())).thenReturn(Optional.empty());
        // when & then
        VisitNotFoundException exception = assertThrows(VisitNotFoundException.class, () -> visitService.assign(visitId, patientId));
        assertEquals("Nie znaleziono terminu wizyty o podanym id", exception.getMessage());
        verify(visitRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(visitRepository);
        verifyNoInteractions(patientRepository, doctorRepository, validator);
    }

    @Test
    void assign_PatientNotFound_PatientNotFoudExceptionThrown() {
        // given
        Long visitId = 1L;
        Long patientId = 1L;
        when(visitRepository.findById(anyLong())).thenReturn(Optional.of(new Visit()));
        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());
        // when & then
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class, () -> visitService.assign(visitId, patientId));
        assertEquals("Nie znaleziono pacjenta o podanym id", exception.getMessage());
        verify(visitRepository, times(1)).findById(1L);
        verify(patientRepository,times(1)).findById(1L);
        verifyNoMoreInteractions(visitRepository, patientRepository);
        verifyNoInteractions(validator, doctorRepository);
    }

    @Test
    void assign_PatientNotNull_VisitAlreadyTakenExceptionThrown() {
        // given
        Visit visit = new Visit();
        Patient patient = new Patient();
        visit.setPatient(patient);
        Long visitId = 1L;
        Long patientId = 1L;
        when(visitRepository.findById(anyLong())).thenReturn(Optional.of(visit));
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));
        // when & then
        VisitAlreadyTakenException exception = assertThrows(VisitAlreadyTakenException.class, () -> visitService.assign(visitId, patientId));
        assertEquals("Ten termin wizyty jest już zajęty", exception.getMessage());
        verify(visitRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(visitRepository, patientRepository);
        verifyNoInteractions(doctorRepository, validator);
    }

    @Test
    void assign_DateExpired_VisitExpiredException() {
        // given
        Visit visit = new Visit();
        visit.setStartDate(LocalDateTime.of(2026, 1, 1, 20, 0, 0));
        visit.setEndDate(LocalDateTime.of(2026, 1, 1, 21, 0, 0));
        Long visitId = 1L;
        Long patientId = 1L;
        when(visitRepository.findById(anyLong())).thenReturn(Optional.of(visit));
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(new Patient()));
        // when & then
        VisitExpiredException exception = assertThrows(VisitExpiredException.class, () -> visitService.assign(visitId, patientId));
        assertEquals("Ten termin wizyty poprzedza aktualną datę i nie jest już dostępny", exception.getMessage());
        verify(visitRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(visitRepository, patientRepository);
        verifyNoInteractions(doctorRepository, validator);
    }

    private CreateVisitCommand makeCreateVisitCommand() {
        return new CreateVisitCommand(1L,
                LocalDateTime.of(2026, 3, 1, 20, 0, 0),
                LocalDateTime.of(2026, 3, 1, 21, 0, 0));
    }

    private Visit createVisit() {
        Visit visit = visitMapper.toEntity(makeCreateVisitCommand());
        visit.setId(1L);
        return visit;
    }

    private CreatePatientCommand makeCreatePatientCommand() {
        return new CreatePatientCommand("email", "123", "55", "Jan", "Kowalski",
                "555555555", LocalDate.of(2007, 11, 25));
    }

    private Patient createPatient() {
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        Patient patient = patientMapper.toEntity(createPatientCommand);
        patient.setId(1L);
        return patient;
    }

    private CreateDoctorCommand makeCreateDoctorCommand() {
        return new CreateDoctorCommand("email", "Jan", "Kowalski",
                "123", Specialization.DERMATOLOGIST);
    }

    private Doctor createDoctor() {
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        Doctor doctor = doctorMapper.toEntity(createDoctorCommand);
        doctor.setId(1L);
        return doctor;
    }
}
