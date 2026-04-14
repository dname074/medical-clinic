package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.authorization.AuthorizationService;
import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.command.CreateDoctorCommand;
import com.dname074.medicalclinic.exception.doctor.DoctorAlreadyExistsException;
import com.dname074.medicalclinic.exception.doctor.DoctorNotFoundException;
import com.dname074.medicalclinic.exception.user.UserAlreadyExistsException;
import com.dname074.medicalclinic.mapper.DoctorMapper;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.model.Doctor;
import com.dname074.medicalclinic.model.Role;
import com.dname074.medicalclinic.model.Specialization;
import com.dname074.medicalclinic.model.User;
import com.dname074.medicalclinic.repository.DoctorRepository;
import com.dname074.medicalclinic.repository.UserRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DoctorServiceTest {
    DoctorService service;
    DoctorRepository doctorRepository;
    UserRepository userRepository;
    DoctorMapper doctorMapper;
    PageMapper pageMapper;
    AuthorizationService authService;

    @BeforeEach
    void setup() {
        this.doctorRepository = Mockito.mock(DoctorRepository.class);
        this.userRepository = Mockito.mock(UserRepository.class);
        this.doctorMapper = Mappers.getMapper(DoctorMapper.class);
        this.pageMapper = Mappers.getMapper(PageMapper.class);
        this.authService = Mockito.mock(AuthorizationService.class);
        this.service = new DoctorService(doctorRepository, userRepository, doctorMapper, pageMapper, authService);
    }

    @Test
    void findAll_DoctorsExists_PageReturned() {
        // given
        Doctor doctor = createDoctor();
        List<Doctor> doctors = List.of(doctor);
        Pageable pageRequest = PageRequest.of(0, 1);
        Page<Doctor> doctorsPage = new PageImpl<>(doctors, pageRequest, 1);
        when(doctorRepository.findAll((Specification<Doctor>) null, pageRequest)).thenReturn(doctorsPage);
        // when
        PageDto<DoctorDto> result = service.findAllDoctors(pageRequest, null);
        // then
        Assertions.assertAll(
                () -> assertEquals(1, result.totalPages()),
                () -> assertEquals(1, result.totalElements()),
                () -> assertFalse(result.content().isEmpty())
        );
        verify(doctorRepository, times(1)).findAll((Specification<Doctor>) null, pageRequest);
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void findAll_DoctorsWithSpecifiedSpecializationExists_PageReturned() {
        // given
        Specialization specialization = Specialization.DERMATOLOGIST;
        Doctor doctor = createDoctor();
        List<Doctor> doctors = List.of(doctor);
        Pageable pageRequest = PageRequest.of(0, 1);
        Page<Doctor> doctorsPage = new PageImpl<>(doctors, pageRequest, 1);
        when(doctorRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(doctorsPage);
        // when
        PageDto<DoctorDto> result = service.findAllDoctors(pageRequest, specialization);
        // then
        Assertions.assertAll(
                () -> assertEquals(1, result.totalPages()),
                () -> assertEquals(1, result.totalElements()),
                () -> assertFalse(result.content().isEmpty())
        );
        verify(doctorRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void getDoctorDtoById_DoctorFoundAndAccessGranted_DoctorReturned() {
        // given
        Long doctorId = 1L;
        Doctor doctor = createDoctorWithUser();
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(jwt.getClaimAsString("sub")).thenReturn("000000000000000000000000000000000000");
        // when
        DoctorDto result = service.getDoctorDtoById(doctorId, auth);
        // then
        Assertions.assertAll(
                () -> assertEquals("email", result.email()),
                () -> assertEquals("Jan", result.user().firstName()),
                () -> assertEquals("Kowalski", result.user().lastName()),
                () -> assertEquals(Specialization.DERMATOLOGIST, result.specialization())
        );
        verify(doctorRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void getDoctorDtoById_DoctorFoundAndCallerIsAdmin_DoctorReturned() {
        // given
        Long doctorId = 1L;
        Doctor doctor = createDoctorWithUser();
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(true);
        when(authService.extractJwt(auth)).thenReturn(jwt);
        // when
        DoctorDto result = service.getDoctorDtoById(doctorId, auth);
        // then
        assertEquals("email", result.email());
        verify(doctorRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void getDoctorDtoById_DoctorFoundButAccessDenied_AccessDeniedExceptionThrown() {
        // given
        Long doctorId = 1L;
        Doctor doctor = createDoctorWithUser();
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(jwt.getClaimAsString("sub")).thenReturn("different-uuid");
        // when & then
        assertThrows(AccessDeniedException.class, () -> service.getDoctorDtoById(doctorId, auth));
        verify(doctorRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void getDoctorDtoById_DoctorNotFound_ExceptionThrown() {
        // given
        Long doctorId = 1L;
        Authentication auth = Mockito.mock(Authentication.class);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());
        // when & then
        DoctorNotFoundException exception = assertThrows(DoctorNotFoundException.class, () -> service.getDoctorDtoById(doctorId, auth));
        assertEquals("Doctor not found", exception.getMessage());
        verify(doctorRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(doctorRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void addDoctor_DoctorNotFound_DoctorAddedAndReturned() {
        // given
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        Doctor doctor = doctorMapper.toEntity(createDoctorCommand);
        User user = new User(null, "000000000000000000000000000000000000", "Jan", "Kowalski");
        doctor.setUser(user);
        when(doctorRepository.findByEmail("email")).thenReturn(Optional.empty());
        when(userRepository.findByFirstNameAndLastName("Jan", "Kowalski")).thenReturn(Optional.empty());
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        // when
        DoctorDto result = service.addDoctor(createDoctorCommand);
        // then
        Assertions.assertAll(
                () -> assertEquals("email", result.email()),
                () -> assertEquals("Jan", result.user().firstName()),
                () -> assertEquals("Kowalski", result.user().lastName()),
                () -> assertEquals(Specialization.DERMATOLOGIST, result.specialization())
        );
        verify(doctorRepository, times(1)).findByEmail("email");
        verify(userRepository, times(1)).findByFirstNameAndLastName("Jan", "Kowalski");
        verify(doctorRepository, times(1)).save(any(Doctor.class));
        verifyNoMoreInteractions(doctorRepository, userRepository);
    }

    @Test
    void addDoctor_DoctorExists_DoctorAlreadyExistsExceptionThrown() {
        // given
        String email = "email";
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        Doctor doctor = doctorMapper.toEntity(createDoctorCommand);
        when(doctorRepository.findByEmail(email)).thenReturn(Optional.of(doctor));
        // when & then
        DoctorAlreadyExistsException exception = assertThrows(DoctorAlreadyExistsException.class, () -> service.addDoctor(createDoctorCommand));
        assertEquals("Doctor with provided email already exists", exception.getMessage());
        verify(doctorRepository, times(1)).findByEmail(email);
        verifyNoMoreInteractions(doctorRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void addDoctor_UserExists_UserAlreadyExistsExceptionThrown() {
        // given
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        User user = new User(null, "000000000000000000000000000000000000", createDoctorCommand.firstName(), createDoctorCommand.lastName());
        when(doctorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByFirstNameAndLastName(createDoctorCommand.firstName(), createDoctorCommand.lastName())).thenReturn(Optional.of(user));
        // when & then
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> service.addDoctor(createDoctorCommand));
        assertEquals("This user has already been added before", exception.getMessage());
        verify(doctorRepository, times(1)).findByEmail("email");
        verify(userRepository, times(1)).findByFirstNameAndLastName("Jan", "Kowalski");
        verifyNoMoreInteractions(doctorRepository, userRepository);
    }

    @Test
    void updateDoctorById_DoctorFoundAndAccessGranted_DoctorUpdatedAndReturned() {
        // given
        Long doctorId = 1L;
        CreateDoctorCommand createDoctorCommandNewData = new CreateDoctorCommand("emailUpdated@onet.pl", "Jan", "Kowalski",
                "123", Specialization.DERMATOLOGIST, "000000000000000000000000000000000000");
        Doctor doctor = createDoctorWithUser();
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(jwt.getClaimAsString("sub")).thenReturn("000000000000000000000000000000000000");
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        // when
        DoctorDto result = service.updateDoctorById(doctorId, createDoctorCommandNewData, auth);
        // then
        Assertions.assertAll(
                () -> assertEquals("emailUpdated@onet.pl", result.email()),
                () -> assertEquals("Jan", result.user().firstName()),
                () -> assertEquals("Kowalski", result.user().lastName()),
                () -> assertEquals(Specialization.DERMATOLOGIST, result.specialization())
        );
        verify(doctorRepository, times(1)).findById(1L);
        verify(doctorRepository, times(1)).save(doctor);
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void updateDoctorById_DoctorFoundButAccessDenied_AccessDeniedExceptionThrown() {
        // given
        Long doctorId = 1L;
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        Doctor doctor = createDoctorWithUser();
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(jwt.getClaimAsString("sub")).thenReturn("different-uuid");
        // when & then
        assertThrows(AccessDeniedException.class, () -> service.updateDoctorById(doctorId, createDoctorCommand, auth));
        verify(doctorRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void updateDoctorById_DoctorNotFound_DoctorNotFoundExceptionThrown() {
        // given
        Long doctorId = 1L;
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        Authentication auth = Mockito.mock(Authentication.class);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());
        // when & then
        DoctorNotFoundException exception = assertThrows(DoctorNotFoundException.class, () -> service.updateDoctorById(doctorId, createDoctorCommand, auth));
        assertEquals("Doctor not found", exception.getMessage());
        verify(doctorRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void deleteDoctorById_DoctorFound_DoctorDeletedAndReturned() {
        // given
        Long doctorId = 1L;
        Doctor doctor = createDoctorWithUser();
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        doNothing().when(doctorRepository).delete(doctor);
        // when
        DoctorDto result = service.deleteDoctorById(doctorId);
        // then
        Assertions.assertAll(
                () -> assertEquals("email", result.email()),
                () -> assertEquals("Jan", result.user().firstName()),
                () -> assertEquals("Kowalski", result.user().lastName()),
                () -> assertEquals(Specialization.DERMATOLOGIST, result.specialization())
        );
        verify(doctorRepository, times(1)).findById(1L);
        verify(doctorRepository, times(1)).delete(doctor);
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void deleteDoctorById_DoctorNotFound_DoctorNotFoundExceptionThrown() {
        // given
        Long doctorId = 1L;
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());
        // when & then
        DoctorNotFoundException exception = assertThrows(DoctorNotFoundException.class, () -> service.deleteDoctorById(doctorId));
        assertEquals("Doctor not found", exception.getMessage());
        verify(doctorRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(doctorRepository);
        verifyNoInteractions(userRepository);
    }

    private CreateDoctorCommand makeCreateDoctorCommand() {
        return new CreateDoctorCommand("email", "Jan", "Kowalski",
                "123", Specialization.DERMATOLOGIST, "000000000000000000000000000000000000");
    }

    private Doctor createDoctor() {
        return doctorMapper.toEntity(makeCreateDoctorCommand());
    }

    private Doctor createDoctorWithUser() {
        CreateDoctorCommand cmd = makeCreateDoctorCommand();
        Doctor doctor = doctorMapper.toEntity(cmd);
        User user = new User(null, cmd.keycloakId(), "Jan", "Kowalski");
        doctor.setUser(user);
        return doctor;
    }
}
