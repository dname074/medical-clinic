package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.authorization.AuthorizationService;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.PatientDto;
import pl.javakurs.dname074.dto.command.ChangePasswordCommand;
import pl.javakurs.dname074.dto.command.CreatePatientCommand;
import com.dname074.medicalclinic.exception.patient.PatientAlreadyExistsException;
import com.dname074.medicalclinic.exception.patient.PatientNotFoundException;
import com.dname074.medicalclinic.exception.user.UserAlreadyExistsException;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.mapper.PatientMapper;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.model.Role;
import com.dname074.medicalclinic.model.User;
import com.dname074.medicalclinic.repository.PatientRepository;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PatientServiceTest {
    PatientService service;
    PatientRepository patientRepository;
    UserRepository userRepository;
    PatientMapper patientMapper;
    PageMapper pageMapper;
    AuthorizationService authService;

    @BeforeEach
    void setup() {
        this.userRepository = Mockito.mock(UserRepository.class);
        this.patientRepository = Mockito.mock(PatientRepository.class);
        this.patientMapper = Mappers.getMapper(PatientMapper.class);
        this.pageMapper = Mappers.getMapper(PageMapper.class);
        this.authService = Mockito.mock(AuthorizationService.class);
        this.service = new PatientService(patientRepository, userRepository, patientMapper, pageMapper, authService);
    }

    @Test
    void findAll_PatientsExists_PageReturned() {
        // given
        Patient patient = createPatient();
        Pageable pageRequest = PageRequest.of(0, 1);
        Page<Patient> page = new PageImpl<>(List.of(patient), pageRequest, 1L);
        when(patientRepository.findAllWithUsers(pageRequest)).thenReturn(page);
        // when
        PageDto<PatientDto> patients = service.findAll(pageRequest);
        // then
        Assertions.assertAll(
                () -> assertEquals(1, patients.totalPages()),
                () -> assertEquals(1, patients.totalElements()),
                () -> assertFalse(patients.content().isEmpty())
        );
        verify(patientRepository, times(1)).findAllWithUsers(pageRequest);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void getPatientDtoById_PatientFoundAndAccessGranted_PatientReturned() {
        // given
        Long id = 1L;
        Patient patient = createPatientWithUser();
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(patientRepository.findById(id)).thenReturn(Optional.of(patient));
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(jwt.getClaimAsString("sub")).thenReturn("000000000000000000000000000000000000");
        // when
        PatientDto result = service.getPatientDtoById(id, auth);
        // then
        Assertions.assertAll(
                () -> assertEquals("email", result.email()),
                () -> assertEquals("Jan", result.user().firstName()),
                () -> assertEquals("Kowalski", result.user().lastName()),
                () -> assertEquals("55", result.idCardNo()),
                () -> assertEquals("555555555", result.phoneNumber()),
                () -> assertEquals(LocalDate.of(2007, 11, 25), result.birthday())
        );
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void getPatientDtoById_PatientFoundAndCallerIsAdmin_PatientReturned() {
        // given
        Long id = 1L;
        Patient patient = createPatientWithUser();
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(patientRepository.findById(id)).thenReturn(Optional.of(patient));
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(true);
        when(authService.extractJwt(auth)).thenReturn(jwt);
        // when
        PatientDto result = service.getPatientDtoById(id, auth);
        // then
        assertEquals("email", result.email());
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void getPatientDtoById_PatientFoundButAccessDenied_AccessDeniedExceptionThrown() {
        // given
        Long id = 1L;
        Patient patient = createPatientWithUser();
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(patientRepository.findById(id)).thenReturn(Optional.of(patient));
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(jwt.getClaimAsString("sub")).thenReturn("different-uuid");
        // when & then
        assertThrows(AccessDeniedException.class, () -> service.getPatientDtoById(id, auth));
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void getPatientDtoById_PatientNotFound_ExceptionThrown() {
        // given
        Long id = 1L;
        Authentication auth = Mockito.mock(Authentication.class);
        when(patientRepository.findById(id)).thenReturn(Optional.empty());
        // when & then
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class, () -> service.getPatientDtoById(id, auth));
        assertEquals("Patient not found", exception.getMessage());
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void addPatient_PatientDoesNotExist_PatientReturned() {
        // given
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        Patient patient = createPatientWithUser();
        when(patientRepository.findByEmail(createPatientCommand.email())).thenReturn(Optional.empty());
        when(userRepository.findByFirstNameAndLastName(createPatientCommand.firstName(), createPatientCommand.lastName())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        // when
        PatientDto result = service.addPatient(createPatientCommand);
        // then
        Assertions.assertAll(
                () -> assertEquals("email", result.email()),
                () -> assertEquals("Jan", result.user().firstName()),
                () -> assertEquals("Kowalski", result.user().lastName()),
                () -> assertEquals("55", result.idCardNo()),
                () -> assertEquals("555555555", result.phoneNumber()),
                () -> assertEquals(LocalDate.of(2007, 11, 25), result.birthday())
        );
        verify(patientRepository, times(1)).findByEmail("email");
        verify(userRepository, times(1)).findByFirstNameAndLastName("Jan", "Kowalski");
        verify(patientRepository, times(1)).save(any(Patient.class));
        verifyNoMoreInteractions(patientRepository, userRepository);
    }

    @Test
    void addPatient_PatientExists_ExceptionThrown() {
        // given
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        Patient patient = createPatientWithUser();
        when(patientRepository.findByEmail(createPatientCommand.email())).thenReturn(Optional.of(patient));
        // when & then
        PatientAlreadyExistsException exception = assertThrows(PatientAlreadyExistsException.class, () -> service.addPatient(createPatientCommand));
        assertEquals("Patient with provided email already exists", exception.getMessage());
        verify(patientRepository, times(1)).findByEmail("email");
        verifyNoMoreInteractions(patientRepository, userRepository);
    }

    @Test
    void addPatient_UserExists_ExceptionThrown() {
        // given
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        User user = new User(null, "000000000000000000000000000000000000", "Jan", "Kowalski");
        when(patientRepository.findByEmail(createPatientCommand.email())).thenReturn(Optional.empty());
        when(userRepository.findByFirstNameAndLastName(createPatientCommand.firstName(), createPatientCommand.lastName())).thenReturn(Optional.of(user));
        // when & then
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> service.addPatient(createPatientCommand));
        assertEquals("This user has already been added before", exception.getMessage());
        verify(patientRepository, times(1)).findByEmail("email");
        verify(userRepository, times(1)).findByFirstNameAndLastName("Jan", "Kowalski");
        verifyNoMoreInteractions(patientRepository, userRepository);
    }

    @Test
    void updatePatientById_DataCorrectAndPatientFoundAndAccessGranted_PatientUpdatedAndReturned() {
        // given
        Long patientId = 1L;
        CreatePatientCommand createPatientCommandNewData = new CreatePatientCommand("newEmail@onet.pl", "123", "55", "Jan", "Kowalski",
                "555555555", LocalDate.of(2007, 11, 25), "000000000000000000000000000000000000");
        Patient patient = createPatientWithUser();
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(jwt.getClaimAsString("sub")).thenReturn("000000000000000000000000000000000000");
        when(patientRepository.save(patient)).thenReturn(patient);
        // when
        PatientDto result = service.updatePatientById(patientId, createPatientCommandNewData, auth);
        // then
        Assertions.assertAll(
                () -> assertEquals("newEmail@onet.pl", result.email()),
                () -> assertEquals("Jan", result.user().firstName()),
                () -> assertEquals("Kowalski", result.user().lastName()),
                () -> assertEquals("55", result.idCardNo()),
                () -> assertEquals("555555555", result.phoneNumber()),
                () -> assertEquals(LocalDate.of(2007, 11, 25), result.birthday())
        );
        verify(patientRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).save(patient);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void updatePatientById_PatientFoundButAccessDenied_AccessDeniedExceptionThrown() {
        // given
        Long patientId = 1L;
        CreatePatientCommand createPatientCommandNewData = new CreatePatientCommand("newEmail@onet.pl", "123", "55", "Jan", "Kowalski",
                "555555555", LocalDate.of(2007, 11, 25), "000000000000000000000000000000000000");
        Patient patient = createPatientWithUser();
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(jwt.getClaimAsString("sub")).thenReturn("different-uuid");
        // when & then
        assertThrows(AccessDeniedException.class, () -> service.updatePatientById(patientId, createPatientCommandNewData, auth));
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void updatePatientById_PatientNotFound_PatientNotFoundExceptionThrown() {
        // given
        Long patientId = 1L;
        CreatePatientCommand createPatientCommandNewData = new CreatePatientCommand("newEmail@onet.pl", "123", "55", "Jan", "Kowalski",
                "555555555", LocalDate.of(2007, 11, 25), "000000000000000000000000000000000000");
        Authentication auth = Mockito.mock(Authentication.class);
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());
        // when & then
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class, () -> service.updatePatientById(patientId, createPatientCommandNewData, auth));
        assertEquals("Patient not found", exception.getMessage());
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void deletePatientById_PatientExists_PatientDeletedAndReturned() {
        // given
        Long patientId = 1L;
        Patient patient = createPatientWithUser();
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        doNothing().when(patientRepository).delete(patient);
        // when
        PatientDto result = service.deletePatientById(patientId);
        // then
        Assertions.assertAll(
                () -> assertEquals("email", result.email()),
                () -> assertEquals("Jan", result.user().firstName()),
                () -> assertEquals("Kowalski", result.user().lastName()),
                () -> assertEquals("55", result.idCardNo()),
                () -> assertEquals("555555555", result.phoneNumber()),
                () -> assertEquals(LocalDate.of(2007, 11, 25), result.birthday())
        );
        verify(patientRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).delete(patient);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void deletePatientById_PatientNotFound_ExceptionThrown() {
        // given
        Long patientId = 1L;
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());
        // when & then
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class, () -> service.deletePatientById(patientId));
        assertEquals("Patient not found", exception.getMessage());
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void modifyPatientPasswordById_PatientFoundAndAccessGranted_PasswordChangedAndPatientReturned() {
        // given
        Long patientId = 1L;
        ChangePasswordCommand changePasswordCommand = new ChangePasswordCommand("newPassword123");
        Patient patient = createPatientWithUser();
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(jwt.getClaimAsString("sub")).thenReturn("000000000000000000000000000000000000");
        when(patientRepository.save(patient)).thenReturn(patient);
        // when
        PatientDto result = service.modifyPatientPasswordById(patientId, changePasswordCommand, auth);
        // then
        Assertions.assertAll(
                () -> assertEquals("email", result.email()),
                () -> assertEquals("Jan", result.user().firstName()),
                () -> assertEquals("Kowalski", result.user().lastName())
        );
        verify(patientRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).save(patient);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void modifyPatientPasswordById_PatientFoundButAccessDenied_AccessDeniedExceptionThrown() {
        // given
        Long patientId = 1L;
        ChangePasswordCommand changePasswordCommand = new ChangePasswordCommand("newPassword123");
        Patient patient = createPatientWithUser();
        Authentication auth = Mockito.mock(Authentication.class);
        Jwt jwt = Mockito.mock(Jwt.class);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(authService.extractJwt(auth)).thenReturn(jwt);
        when(authService.hasRole(auth, Role.ADMIN)).thenReturn(false);
        when(jwt.getClaimAsString("sub")).thenReturn("different-uuid");
        // when & then
        assertThrows(AccessDeniedException.class, () -> service.modifyPatientPasswordById(patientId, changePasswordCommand, auth));
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void modifyPatientPasswordById_PatientNotFound_PatientNotFoundExceptionThrown() {
        // given
        Long patientId = 1L;
        ChangePasswordCommand changePasswordCommand = new ChangePasswordCommand("12345");
        Authentication auth = Mockito.mock(Authentication.class);
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());
        // when & then
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class, () -> service.modifyPatientPasswordById(patientId, changePasswordCommand, auth));
        assertEquals("Patient not found", exception.getMessage());
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(userRepository);
    }

    private CreatePatientCommand makeCreatePatientCommand() {
        return new CreatePatientCommand("email", "123", "55", "Jan", "Kowalski",
                "555555555", LocalDate.of(2007, 11, 25), "000000000000000000000000000000000000");
    }

    private Patient createPatient() {
        return patientMapper.toEntity(makeCreatePatientCommand());
    }

    private Patient createPatientWithUser() {
        Patient patient = patientMapper.toEntity(makeCreatePatientCommand());
        User user = new User(null, "000000000000000000000000000000000000", "Jan", "Kowalski");
        patient.setUser(user);
        return patient;
    }
}
