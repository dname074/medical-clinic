package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.dto.PatientDto;
import com.dname074.medicalclinic.dto.command.CreatePatientCommand;
import com.dname074.medicalclinic.exception.patient.PatientAlreadyExistsException;
import com.dname074.medicalclinic.exception.patient.PatientNotFoundException;
import com.dname074.medicalclinic.exception.user.UserAlreadyExistsException;
import com.dname074.medicalclinic.mapper.PatientMapper;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.model.User;
import com.dname074.medicalclinic.repository.PatientRepository;
import com.dname074.medicalclinic.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class PatientServiceTest {
    PatientService service;
    PatientRepository patientRepository;
    UserRepository userRepository;
    PatientMapper patientMapper;

    @BeforeEach
    void setup() {
        this.userRepository = Mockito.mock(UserRepository.class);
        this.patientRepository = Mockito.mock(PatientRepository.class);
        this.patientMapper = Mappers.getMapper(PatientMapper.class);
        this.service = new PatientService(patientRepository, userRepository, patientMapper);
    }

    @Test
    void findAll_PatientsExists_PageReturned() {
        // given
        Patient patient = createPatient();
        Pageable pageRequest = PageRequest.of(0, 1);
        Page page = new PageImpl(List.of(patient), pageRequest, 1L);
        when(patientRepository.findAllWithUsers(pageRequest)).thenReturn(page);
        // when
        Page<PatientDto> patients = service.findAll(pageRequest);
        // then
        Assertions.assertAll(
                () -> assertEquals(1, patients.getTotalPages()),
                () -> assertEquals(1, patients.getTotalElements()),
                () -> assertFalse(patients.getContent().isEmpty())
        );
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
    }

    @Test
    void getPatientDtoById_PatientNotFound_ExceptionThrown() {
        // given
        Long id = 1L;
        when(patientRepository.findById(id)).thenReturn(Optional.empty());
        // when
        Executable executable = () -> service.getPatientDtoById(id);
        // then
        assertThrowsExactly(PatientNotFoundException.class, executable);
    }

    @Test
    void addPatient_PatientNotExists_PatientReturned() {
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
    }

    @Test
    void addPatient_PatientExists_ExceptionThrown() {
        // given
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        Patient patient = patientMapper.toEntity(createPatientCommand);
        when(patientRepository.findByEmail(createPatientCommand.email())).thenReturn(Optional.of(patient));
        when(userRepository.findByFirstNameAndLastName(createPatientCommand.firstName(), createPatientCommand.lastName())).thenReturn(Optional.empty());
        // when
        Executable executable = () -> service.addPatient(createPatientCommand);
        // then
        // na poczatku mialem tak:
        // assertThrowsExactly(PatientAlreadyExistsException.class, () -> service.addPatient(createPatientCommand));
        // lecz wtedy nie bylo jasnego podzialu na sekcje 'when' i 'then' wiec wynioslem tą funkcje to obiektu, a nie przekazywałem od razu w parametrze
        assertThrowsExactly(PatientAlreadyExistsException.class, executable);
    }

    @Test
    void addPatient_UserExists_ExceptionThrown() {
        // given
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        Patient patient = patientMapper.toEntity(createPatientCommand);
        User user = patient.getUser();
        when(userRepository.findByFirstNameAndLastName(createPatientCommand.firstName(), createPatientCommand.lastName())).thenReturn(Optional.of(user));
        // when
        Executable executable = () -> service.addPatient(createPatientCommand);
        // then
        assertThrowsExactly(UserAlreadyExistsException.class, executable);
    }

//    @Test
//    void deletePatientById_PatientExists_PatientDeletedAndReturned() {
//        // bedzie zrobione
//    }

    @Test
    void updatePatientById_DataCorrect_PatientUpdatedAndReturned() {
        // given
        Long patientId = 1L;
        CreatePatientCommand createPatientCommand = makeCreatePatientCommand();
        Patient patient = patientMapper.toEntity(createPatientCommand);
        doNothing().when(patientRepository).save(patient);
        // when
        PatientDto result = service.updatePatientById(patientId, createPatientCommand);
        // then
        Assertions.assertAll(
                () -> assertEquals("email", result.email()),
                () -> assertEquals("Jan", result.user().firstName()),
                () -> assertEquals("Kowalski", result.user().lastName()),
                () -> assertEquals("55", result.idCardNo()),
                () -> assertEquals("555555555", result.phoneNumber()),
                () -> assertEquals(LocalDate.of(2007, 11, 25), result.birthday())
        );
    }

//    @Test
//    void modifypatientPasswordById() {
//        // bedzie zrobione
//    }

    @Test
    void getPatientById_PatientFound_PatientReturned() {
        // given
        Long patientId = 1L;
        Patient patient = createPatient();
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        // when
        Patient result = service.getPatientById(patientId);
        // then
        Assertions.assertAll(
                () -> assertEquals("email", result.getEmail()),
                () -> assertEquals("Jan", result.getUser().getFirstName()),
                () -> assertEquals("Kowalski", result.getUser().getLastName()),
                () -> assertEquals("55", result.getIdCardNo()),
                () -> assertEquals("555555555", result.getPhoneNumber()),
                () -> assertEquals(LocalDate.of(2007, 11, 25), result.getBirthday())
        );
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
