package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.argumentmatcher.PatientArgumentMatcher;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.PatientDto;
import com.dname074.medicalclinic.dto.command.ChangePasswordCommand;
import com.dname074.medicalclinic.dto.command.CreatePatientCommand;
import com.dname074.medicalclinic.exception.patient.PatientAlreadyExistsException;
import com.dname074.medicalclinic.exception.patient.PatientNotFoundException;
import com.dname074.medicalclinic.exception.user.UserAlreadyExistsException;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.mapper.PatientMapper;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.model.User;
import com.dname074.medicalclinic.repository.PatientRepository;
import com.dname074.medicalclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
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

    @BeforeEach
    void setup() {
        this.userRepository = Mockito.mock(UserRepository.class);
        this.patientRepository = Mockito.mock(PatientRepository.class);
        this.patientMapper = Mappers.getMapper(PatientMapper.class);
        this.pageMapper = Mappers.getMapper(PageMapper.class);
        this.service = new PatientService(patientRepository, userRepository, patientMapper, pageMapper);
    }

    @Test
    void findAll_PatientsExists_PageReturned() {
        // given
        Patient patient = createPatient();
        Pageable pageRequest = PageRequest.of(0, 1);
        Page page = new PageImpl(List.of(patient), pageRequest, 1L);
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
    void getPatientDtoById_PatientFound_PatientReturned() {
        // given
        Long id = 1L;
        Patient patient = createPatient();
        when(patientRepository.findById(id)).thenReturn(Optional.of(patient));
        // when
        PatientDto result = service.getPatientDtoById(id);
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
    void getPatientDtoById_PatientNotFound_ExceptionThrown() {
        // given
        Long id = 1L;
        when(patientRepository.findById(id)).thenReturn(Optional.empty());
        // when i then
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class, () -> service.getPatientDtoById(id));
        assertEquals("Nie udało się znaleźć pacjenta o podanym id", exception.getMessage());
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void addPatient_PatientDoesNotExist_PatientReturned() {
        // given
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        Patient patient = patientMapper.toEntity(createPatientCommand);
        when(patientRepository.findByEmail(createPatientCommand.email())).thenReturn(Optional.empty());
        when(userRepository.findByFirstNameAndLastName(createPatientCommand.firstName(), createPatientCommand.lastName())).thenReturn(Optional.empty());
        when(patientRepository.save(patient)).thenReturn(patient);
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
        verify(userRepository,times(1)).findByFirstNameAndLastName("Jan", "Kowalski");
        verify(patientRepository,times(1)).save(argThat(new PatientArgumentMatcher(patient)));
        verifyNoMoreInteractions(patientRepository, userRepository);
    }

    @Test
    void addPatient_PatientExists_ExceptionThrown() {
        // given
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        Patient patient = patientMapper.toEntity(createPatientCommand);
        when(patientRepository.findByEmail(createPatientCommand.email())).thenReturn(Optional.of(patient));
        // when i then
        PatientAlreadyExistsException exception = assertThrows(PatientAlreadyExistsException.class, () -> service.addPatient(createPatientCommand));
        assertEquals("Pacjent o podanym adresie email już istnieje w bazie danych", exception.getMessage());
        verify(patientRepository, times(1)).findByEmail("email");
        verifyNoMoreInteractions(patientRepository, userRepository);
    }

    @Test
    void addPatient_UserExists_ExceptionThrown() {
        // given
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        Patient patient = patientMapper.toEntity(createPatientCommand);
        User user = patient.getUser();
        when(patientRepository.findByEmail(patient.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByFirstNameAndLastName(createPatientCommand.firstName(), createPatientCommand.lastName())).thenReturn(Optional.of(user));
        // when i then
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> service.addPatient(createPatientCommand));
        assertEquals("Ta osoba została już dodana do systemu", exception.getMessage());
        verify(patientRepository, times(1)).findByEmail("email");
        verify(userRepository, times(1)).findByFirstNameAndLastName("Jan", "Kowalski");
        verifyNoMoreInteractions(patientRepository, userRepository);
    }

    @Test
    void deletePatientById_PatientExists_PatientDeletedAndReturned() {
        // given
        Long patientId = 1L;
        Patient patient = createPatient();
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
        Patient patient = createPatient();
        when(patientRepository.findById(patientId)).thenThrow(PatientNotFoundException.class);
        doNothing().when(patientRepository).delete(patient);
        // when i then
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,() -> service.deletePatientById(patientId));
        assertEquals("Nie udało się znaleźć pacjenta o podanym id", exception.getMessage());
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void updatePatientById_DataCorrectAndPatientFound_PatientUpdatedAndReturned() {
        // given
        Long patientId = 1L;
        CreatePatientCommand createPatientCommandNewData = new CreatePatientCommand("newEmail@onet.pl", "123", "55", "Jan", "Kowalski",
                "555555555", LocalDate.of(2007, 11, 25));
        Patient patient = createPatient();
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        // when
        PatientDto result = service.updatePatientById(patientId, createPatientCommandNewData);
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
        verify(patientRepository, times(1)).save(argThat(new PatientArgumentMatcher(patient)));
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void updatePatientById_PatientNotFound_PatientNotFoundExceptionThrown() {
        // given
        Long patientId = 1L;
        CreatePatientCommand createPatientCommandNewData = new CreatePatientCommand("newEmail@onet.pl", "123", "55", "Jan", "Kowalski",
                "555555555", LocalDate.of(2007, 11, 25));
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());
        // when & then
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class, () -> service.updatePatientById(1L, createPatientCommandNewData));
        assertEquals("Nie udało się znaleźć pacjenta o podanym id", exception.getMessage());
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void modifyPatientPasswordById_PatientFound_PatientModifiedAndReturned() {
        // given
        Long patientId = 1L;
        String password = "12345";
        CreatePatientCommand createPatientCommandNewData = new CreatePatientCommand("newEmail@onet.pl", password, "55", "Jan", "Kowalski",
                "555555555", LocalDate.of(2007, 11, 25));
        Patient patient = createPatient();
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        // when
        PatientDto result = service.updatePatientById(patientId, createPatientCommandNewData);
        // then
        Assertions.assertAll(
                () -> assertEquals("newEmail@onet.pl", result.email()),
                () -> assertEquals("Jan", result.user().firstName()),
                () -> assertEquals("Kowalski", result.user().lastName()),
                () -> assertEquals("55", result.idCardNo()),
                () -> assertEquals("555555555", result.phoneNumber()),
                () -> assertEquals(LocalDate.of(2007, 11, 25), result.birthday()),
                () -> assertEquals("12345", patient.getPassword())
        );
        verify(patientRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).save(argThat(new PatientArgumentMatcher(patient)));
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void modifyPatientPasswordById_PatientNotFound_PatientNotFoundExceptionThrown() {
        // given
        Long patientId = 1L;
        ChangePasswordCommand changePasswordCommand = new ChangePasswordCommand("12345");
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());
        // when & then
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class, () -> service.modifyPatientPasswordById(patientId, changePasswordCommand));
        assertEquals("Nie udało się znaleźć pacjenta o podanym id", exception.getMessage());
        verify(patientRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(patientRepository);
        verifyNoInteractions(userRepository);
    }

    private CreatePatientCommand makeCreatePatientCommand() {
        return new CreatePatientCommand("email", "123", "55", "Jan", "Kowalski",
                "555555555", LocalDate.of(2007, 11, 25));
    }

    private Patient createPatient() {
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        return patientMapper.toEntity(createPatientCommand);
    }
}
