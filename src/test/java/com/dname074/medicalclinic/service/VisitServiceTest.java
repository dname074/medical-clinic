package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.argumentmatcher.VisitArgumentMatcher;
import com.dname074.medicalclinic.authorization.AuthorizationService;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.VisitDto;
import pl.javakurs.dname074.dto.command.CreateDoctorCommand;
import pl.javakurs.dname074.dto.command.CreatePatientCommand;
import pl.javakurs.dname074.dto.command.CreateVisitCommand;
import com.dname074.medicalclinic.exception.doctor.DoctorNotFoundException;
import com.dname074.medicalclinic.exception.patient.PatientNotFoundException;
import com.dname074.medicalclinic.exception.visit.InvalidVisitException;
import com.dname074.medicalclinic.exception.visit.VisitAlreadyCanceledException;
import com.dname074.medicalclinic.exception.visit.VisitAlreadyTakenException;
import com.dname074.medicalclinic.exception.visit.VisitExpiredException;
import com.dname074.medicalclinic.exception.visit.VisitNotFoundException;
import com.dname074.medicalclinic.mapper.DoctorMapper;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.mapper.PatientMapper;
import com.dname074.medicalclinic.mapper.VisitMapper;
import com.dname074.medicalclinic.model.Doctor;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.model.Role;
import com.dname074.medicalclinic.model.Specialization;
import com.dname074.medicalclinic.model.User;
import com.dname074.medicalclinic.model.Visit;
import com.dname074.medicalclinic.model.VisitStatus;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
    PatientMapper patientMapper;
    DoctorMapper doctorMapper;
    AuthorizationService authService;

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
        this.authService = Mockito.mock(AuthorizationService.class);
        this.visitService = new VisitService(visitRepository, doctorRepository, patientRepository,
                visitMapper, validator, pageMapper, currentDate, authService);
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
        when(visitRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        // when
        PageDto<VisitDto> result = visitService.getVisitsByPatientId(patientId, pageable);
        // then
        Assertions.assertAll(
                () -> assertEquals(page.getTotalPages(), result.totalPages()),
                () -> assertEquals(page.getTotalElements(), result.totalElements()),
                () -> assertFalse(result.content().isEmpty())
        );
        verify(visitRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verifyNoMoreInteractions(visitRepository);
        verifyNoInteractions(doctorRepository, patientRepository);
    }

    @Test
    void getVisitsByDoctorId_AvailableStatusAndCallerIsAdmin_PageReturned() {
        // given
        Long doctorId = 1L;
        Doctor doctor = createDoctorWithUser();
        doctor.setId(doctorId);
        Visit visit = createVisit();
        visit.setDoctor(doctor);
        List<Visit> visits = List.of(visit);
        Pageable pageable = PageRequest.of(0, 1);
        Page<Visit> page = new PageImpl<>(visits, pageable, 1);
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(true);
        when(jwt.getClaimAsString("sub")).thenReturn("some-uuid");
        when(visitRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        // when
        PageDto<VisitDto> result = visitService.getVisitsByDoctorId(doctorId, VisitStatus.AVAILABLE, pageable, auth);
        // then
        Assertions.assertAll(
                () -> assertEquals(page.getTotalPages(), result.totalPages()),
                () -> assertEquals(page.getTotalElements(), result.totalElements()),
                () -> assertFalse(result.content().isEmpty())
        );
        verify(doctorRepository, times(1)).findById(doctorId);
        verify(visitRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verifyNoMoreInteractions(visitRepository, doctorRepository);
        verifyNoInteractions(patientRepository);
    }

    @Test
    void getVisitsByDoctorId_PatientRequestsNonAvailableVisits_AccessDeniedExceptionThrown() {
        // given
        Long doctorId = 1L;
        Doctor doctor = createDoctorWithUser();
        doctor.setId(doctorId);
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(authService.hasRole(auth, Role.PATIENT)).thenReturn(true);
        when(jwt.getClaimAsString("sub")).thenReturn("000000000000000000000000000000000000");
        // when & then
        assertThrows(AccessDeniedException.class,
                () -> visitService.getVisitsByDoctorId(doctorId, VisitStatus.BOOKED, pageable(), auth));
        verify(doctorRepository, times(1)).findById(doctorId);
        verifyNoMoreInteractions(doctorRepository);
        verifyNoInteractions(visitRepository, patientRepository);
    }

    @Test
    void getVisitsByDoctorId_DoctorNotFound_DoctorNotFoundExceptionThrown() {
        // given
        Long doctorId = 1L;
        Authentication auth = Mockito.mock(Authentication.class);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());
        // when & then
        DoctorNotFoundException exception = assertThrows(DoctorNotFoundException.class,
                () -> visitService.getVisitsByDoctorId(doctorId, VisitStatus.AVAILABLE, pageable(), auth));
        assertEquals("Doctor not found", exception.getMessage());
        verify(doctorRepository, times(1)).findById(doctorId);
        verifyNoMoreInteractions(doctorRepository);
        verifyNoInteractions(visitRepository, patientRepository);
    }

    @Test
    void getFilteredVisits_VisitFoundBySpecializationAndDate_PageReturned() {
        // given
        Specialization doctorSpecialization = Specialization.DERMATOLOGIST;
        LocalDate fromDate = LocalDate.of(2027, 1, 1);
        LocalDate toDate = LocalDate.of(2027, 5, 1);
        Doctor doctor = createDoctor();
        Visit visit = createVisit();
        visit.setDoctor(doctor);
        List<Visit> visits = List.of(visit);
        Pageable pageable = PageRequest.of(0, 1);
        Page<Visit> page = new PageImpl<>(visits, pageable, 1);
        Authentication auth = Mockito.mock(Authentication.class);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(true);
        when(visitRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        // when
        PageDto<VisitDto> result = visitService.getFilteredVisits(fromDate, toDate, doctorSpecialization, VisitStatus.AVAILABLE, pageable, auth);
        // then
        Assertions.assertAll(
                () -> assertEquals(page.getTotalPages(), result.totalPages()),
                () -> assertEquals(page.getTotalElements(), result.totalElements()),
                () -> assertFalse(result.content().isEmpty())
        );
        verify(visitRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verifyNoMoreInteractions(visitRepository);
        verifyNoInteractions(doctorRepository, patientRepository);
    }

    @Test
    void getFilteredVisits_PatientRequestsNonAvailableVisits_AccessDeniedExceptionThrown() {
        // given
        LocalDate fromDate = LocalDate.of(2027, 1, 1);
        LocalDate toDate = LocalDate.of(2027, 5, 1);
        Authentication auth = Mockito.mock(Authentication.class);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(authService.hasRole(auth, Role.PATIENT)).thenReturn(true);
        // when & then
        assertThrows(AccessDeniedException.class,
                () -> visitService.getFilteredVisits(fromDate, toDate, null, VisitStatus.BOOKED, pageable(), auth));
        verifyNoInteractions(visitRepository, doctorRepository, patientRepository);
    }

    @Test
    void addAvailableVisit_DoctorFoundAndAccessGrantedAndVisitDateCorrect_VisitReturned() {
        // given
        Long doctorId = 1L;
        CreateVisitCommand createVisitCommand = makeCreateVisitCommand();
        Doctor doctor = createDoctorWithUser();
        doctor.setId(doctorId);
        doctor.setVisits(new ArrayList<>());
        Visit savedVisit = createVisit();
        savedVisit.setDoctor(doctor);
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(jwt.getClaimAsString("sub")).thenReturn("000000000000000000000000000000000000");
        doNothing().when(validator).validateVisitDate(any(), any());
        when(visitRepository.save(any())).thenReturn(savedVisit);
        // when
        VisitDto result = visitService.addAvailableVisit(createVisitCommand, auth);
        // then
        Assertions.assertAll(
                () -> assertEquals(LocalDateTime.of(2026, 3, 1, 20, 0, 0), result.startDate()),
                () -> assertEquals(LocalDateTime.of(2026, 3, 1, 21, 0, 0), result.endDate()),
                () -> assertEquals(VisitStatus.AVAILABLE, result.visitStatus())
        );
        verify(doctorRepository, times(1)).findById(1L);
        verify(validator, times(1)).validateVisitDate(any(), any());
        verify(visitRepository, times(1)).save(any());
        verifyNoMoreInteractions(visitRepository, doctorRepository, validator);
        verifyNoInteractions(patientRepository);
    }

    @Test
    void addAvailableVisit_DoctorFoundButAccessDenied_AccessDeniedExceptionThrown() {
        // given
        Long doctorId = 1L;
        CreateVisitCommand createVisitCommand = makeCreateVisitCommand();
        Doctor doctor = createDoctorWithUser();
        doctor.setId(doctorId);
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(jwt.getClaimAsString("sub")).thenReturn("different-uuid");
        // when & then
        assertThrows(AccessDeniedException.class, () -> visitService.addAvailableVisit(createVisitCommand, auth));
        verify(doctorRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(doctorRepository);
        verifyNoInteractions(visitRepository, patientRepository);
    }

    @Test
    void addAvailableVisit_DoctorNotFound_DoctorNotFoundExceptionThrown() {
        // given
        CreateVisitCommand createVisitCommand = makeCreateVisitCommand();
        Authentication auth = Mockito.mock(Authentication.class);
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());
        // when & then
        DoctorNotFoundException exception = assertThrows(DoctorNotFoundException.class, () -> visitService.addAvailableVisit(createVisitCommand, auth));
        assertEquals("Doctor not found", exception.getMessage());
        verify(doctorRepository, times(1)).findById(createVisitCommand.doctorId());
        verifyNoMoreInteractions(doctorRepository);
        verifyNoInteractions(patientRepository, visitRepository, validator);
    }

    @Test
    void addAvailableVisit_DateIncorrect_InvalidVisitExceptionThrown() {
        // given
        CreateVisitCommand createVisitCommand = makeCreateVisitCommand();
        Doctor doctor = createDoctorWithUser();
        doctor.setId(1L);
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(doctor));
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(jwt.getClaimAsString("sub")).thenReturn("000000000000000000000000000000000000");
        doThrow(InvalidVisitException.class).when(validator).validateVisitDate(any(), any());
        // when & then
        assertThrows(InvalidVisitException.class, () -> visitService.addAvailableVisit(createVisitCommand, auth));
        verify(doctorRepository, times(1)).findById(anyLong());
        verify(validator, times(1)).validateVisitDate(any(), any());
        verifyNoMoreInteractions(validator, doctorRepository);
        verifyNoInteractions(patientRepository, visitRepository);
    }

    @Test
    void assign_VisitFoundPatientFoundAndDateNotExpired_VisitAssignedAndVisitReturned() {
        // given
        Long visitId = 1L;
        Long patientId = 1L;
        Visit visit = createVisit();
        visit.setVisitStatus(VisitStatus.AVAILABLE);
        visit.setStartDate(LocalDateTime.of(2026, 3, 1, 20, 0, 0));
        Patient patient = createPatient();
        patient.setVisits(new ArrayList<>());
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(true);
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(visitRepository.save(visit)).thenReturn(visit);
        // when
        VisitDto result = visitService.assign(visitId, patientId, auth);
        // then
        Assertions.assertAll(
                () -> assertEquals(visit.getStartDate(), result.startDate()),
                () -> assertEquals(visit.getEndDate(), result.endDate()),
                () -> assertEquals(VisitStatus.BOOKED, result.visitStatus())
        );
        verify(visitRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).findById(1L);
        verify(visitRepository, times(1)).save(visit);
        verifyNoMoreInteractions(visitRepository, patientRepository);
        verifyNoInteractions(doctorRepository);
    }

    @Test
    void assign_PatientAttemptsToBookForSomeoneElse_AccessDeniedExceptionThrown() {
        // given
        Long visitId = 1L;
        Long patientId = 1L;
        Visit visit = createVisit();
        visit.setVisitStatus(VisitStatus.AVAILABLE);
        Patient patient = createPatientWithUser();
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn("different-uuid");
        // when & then
        assertThrows(AccessDeniedException.class, () -> visitService.assign(visitId, patientId, auth));
        verify(visitRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(visitRepository, patientRepository);
        verifyNoInteractions(doctorRepository);
    }

    @Test
    void assign_VisitNotFound_VisitNotFoundExceptionThrown() {
        // given
        Long visitId = 1L;
        Long patientId = 1L;
        Authentication auth = Mockito.mock(Authentication.class);
        when(visitRepository.findById(anyLong())).thenReturn(Optional.empty());
        // when & then
        VisitNotFoundException exception = assertThrows(VisitNotFoundException.class, () -> visitService.assign(visitId, patientId, auth));
        assertEquals("Nie znaleziono terminu wizyty o podanym id", exception.getMessage());
        verify(visitRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(visitRepository);
        verifyNoInteractions(patientRepository, doctorRepository, validator);
    }

    @Test
    void assign_PatientNotFound_PatientNotFoundExceptionThrown() {
        // given
        Long visitId = 1L;
        Long patientId = 1L;
        Authentication auth = Mockito.mock(Authentication.class);
        when(visitRepository.findById(anyLong())).thenReturn(Optional.of(new Visit()));
        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());
        // when & then
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class, () -> visitService.assign(visitId, patientId, auth));
        assertEquals("Nie znaleziono pacjenta o podanym id", exception.getMessage());
        verify(visitRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(visitRepository, patientRepository);
        verifyNoInteractions(validator, doctorRepository);
    }

    @Test
    void assign_VisitAlreadyBooked_VisitAlreadyTakenExceptionThrown() {
        // given
        Visit visit = new Visit();
        Patient patient = new Patient();
        visit.setPatient(patient);
        visit.setVisitStatus(VisitStatus.BOOKED);
        Long visitId = 1L;
        Long patientId = 1L;
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(visitRepository.findById(anyLong())).thenReturn(Optional.of(visit));
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(true);
        when(authService.extractJwt(auth)).thenReturn(jwt);
        // when & then
        VisitAlreadyTakenException exception = assertThrows(VisitAlreadyTakenException.class, () -> visitService.assign(visitId, patientId, auth));
        assertEquals("Ten termin wizyty jest już zajęty", exception.getMessage());
        verify(visitRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(visitRepository, patientRepository);
        verifyNoInteractions(doctorRepository, validator);
    }

    @Test
    void assign_DateExpired_VisitExpiredExceptionThrown() {
        // given
        Visit visit = new Visit();
        visit.setStartDate(LocalDateTime.of(2026, 1, 1, 20, 0, 0));
        visit.setEndDate(LocalDateTime.of(2026, 1, 1, 21, 0, 0));
        visit.setVisitStatus(VisitStatus.AVAILABLE);
        Long visitId = 1L;
        Long patientId = 1L;
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(visitRepository.findById(anyLong())).thenReturn(Optional.of(visit));
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(new Patient()));
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(true);
        when(authService.extractJwt(auth)).thenReturn(jwt);
        // when & then
        VisitExpiredException exception = assertThrows(VisitExpiredException.class, () -> visitService.assign(visitId, patientId, auth));
        assertEquals("Ten termin wizyty poprzedza aktualną datę i nie jest już dostępny", exception.getMessage());
        verify(visitRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(visitRepository, patientRepository);
        verifyNoInteractions(doctorRepository, validator);
    }

    @Test
    void cancelVisit_VisitFoundAndCallerIsOwnerDoctor_VisitCanceledAndReturned() {
        // given
        Long visitId = 1L;
        Doctor doctor = createDoctorWithUser();
        Visit visit = createVisit();
        visit.setDoctor(doctor);
        visit.setVisitStatus(VisitStatus.BOOKED);
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn("000000000000000000000000000000000000");
        when(visitRepository.save(any())).thenReturn(visit);
        // when
        VisitDto result = visitService.cancelVisit(visitId, auth);
        // then
        Assertions.assertAll(
                () -> assertEquals(visit.getStartDate(), result.startDate()),
                () -> assertEquals(visit.getEndDate(), result.endDate()),
                () -> assertEquals(VisitStatus.CANCELED, result.visitStatus())
        );
        verify(visitRepository, times(1)).findById(1L);
        verify(visitRepository, times(1)).save(argThat(new VisitArgumentMatcher(visit)));
        verifyNoMoreInteractions(visitRepository);
        verifyNoInteractions(doctorRepository, patientRepository);
    }

    @Test
    void cancelVisit_VisitFoundButCallerIsNotOwnerDoctor_AccessDeniedExceptionThrown() {
        // given
        Long visitId = 1L;
        Doctor doctor = createDoctorWithUser();
        Visit visit = createVisit();
        visit.setDoctor(doctor);
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn("different-uuid");
        // when & then
        assertThrows(AccessDeniedException.class, () -> visitService.cancelVisit(visitId, auth));
        verify(visitRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(visitRepository);
        verifyNoInteractions(doctorRepository, patientRepository);
    }

    @Test
    void cancelVisit_VisitNotFound_VisitNotFoundExceptionThrown() {
        // given
        Long visitId = 1L;
        Authentication auth = Mockito.mock(Authentication.class);
        when(visitRepository.findById(visitId)).thenReturn(Optional.empty());
        // when & then
        VisitNotFoundException exception = assertThrows(VisitNotFoundException.class, () -> visitService.cancelVisit(visitId, auth));
        assertEquals("Visit with provided id does not exist", exception.getMessage());
        verify(visitRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(visitRepository);
        verifyNoInteractions(doctorRepository, patientRepository);
    }

    @Test
    void cancelVisit_VisitFoundButAlreadyCanceled_VisitAlreadyCanceledExceptionThrown() {
        // given
        Long visitId = 1L;
        Doctor doctor = createDoctorWithUser();
        Visit visit = createVisit();
        visit.setDoctor(doctor);
        visit.setVisitStatus(VisitStatus.CANCELED);
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn("000000000000000000000000000000000000");
        // when & then
        VisitAlreadyCanceledException exception = assertThrows(VisitAlreadyCanceledException.class, () -> visitService.cancelVisit(visitId, auth));
        assertEquals("This visit has already been canceled before", exception.getMessage());
        verify(visitRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(visitRepository);
        verifyNoInteractions(doctorRepository, patientRepository);
    }

    private Pageable pageable() {
        return PageRequest.of(0, 10);
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
        return new CreatePatientCommand("email",  "123", "55", "Jan", "Kowalski",
                "555555555", LocalDate.of(2007, 11, 25), "000000000000000000000000000000000000");
    }

    private Patient createPatient() {
        Patient patient = patientMapper.toEntity(makeCreatePatientCommand());
        patient.setId(1L);
        return patient;
    }

    private Patient createPatientWithUser() {
        Patient patient = createPatient();
        User user = new User(null, "000000000000000000000000000000000000", "Jan", "Kowalski");
        patient.setUser(user);
        return patient;
    }

    private CreateDoctorCommand makeCreateDoctorCommand() {
        return new CreateDoctorCommand("email", "Jan", "Kowalski",
                "123", Specialization.DERMATOLOGIST, "000000000000000000000000000000000000");
    }

    private Doctor createDoctor() {
        Doctor doctor = doctorMapper.toEntity(makeCreateDoctorCommand());
        doctor.setId(1L);
        return doctor;
    }

    private Doctor createDoctorWithUser() {
        Doctor doctor = createDoctor();
        User user = new User(null, "000000000000000000000000000000000000", "Jan", "Kowalski");
        doctor.setUser(user);
        return doctor;
    }
}
