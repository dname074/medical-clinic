package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.argumentmatcher.DoctorArgumentMatcher;
import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.command.CreateDoctorCommand;
import com.dname074.medicalclinic.exception.doctor.DoctorAlreadyExistsException;
import com.dname074.medicalclinic.exception.doctor.DoctorNotFoundException;
import com.dname074.medicalclinic.exception.user.UserAlreadyExistsException;
import com.dname074.medicalclinic.mapper.DoctorMapper;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.model.Doctor;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
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

    @BeforeEach
    void setup() {
        this.doctorRepository = Mockito.mock(DoctorRepository.class);
        this.userRepository = Mockito.mock(UserRepository.class);
        this.doctorMapper = Mappers.getMapper(DoctorMapper.class);
        this.pageMapper = Mappers.getMapper(PageMapper.class);
        this.service = new DoctorService(doctorRepository, userRepository, doctorMapper, pageMapper);
    }

    @Test
    void findAll_DoctorsExists_PageReturned() {
        // given
        Doctor doctor = createDoctor();
        List<Doctor> doctors = List.of(doctor);
        Pageable pageRequest = PageRequest.of(0, 1);
        Page<Doctor> doctorsPage = new PageImpl<>(doctors, pageRequest, 1);
        when(doctorRepository.findAllWithUsers(pageRequest)).thenReturn(doctorsPage);
        // when
        PageDto<DoctorDto> result = service.findAllDoctors(pageRequest);
        // then
        Assertions.assertAll(
                () -> assertEquals(1, result.totalPages()),
                () -> assertEquals(1, result.totalElements()),
                () -> assertFalse(result.content().isEmpty())
        );
        verify(doctorRepository, times(1)).findAllWithUsers(pageRequest);
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void getDoctorDtoById_DoctorFound_DoctorReturned() {
        // given
        Long doctorId = 1L;
        Doctor doctor = createDoctor();
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        // when
        DoctorDto result = service.getDoctorDtoById(doctorId);
        // then
        Assertions.assertAll(
                () -> assertEquals("email", result.email()),
                () -> assertEquals("Jan", result.user().firstName()),
                () -> assertEquals("Kowalski", result.user().lastName()),
                () -> assertEquals("123", doctor.getPassword()),
                () -> assertEquals(Specialization.DERMATOLOGIST, result.specialization())
        );
        verify(doctorRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void getDoctorDtoById_DoctorNotFound_ExceptionThrown() {
        // given
        Long doctorId = 1L;
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());
        // when
        DoctorNotFoundException exception = assertThrows(DoctorNotFoundException.class, () -> service.getDoctorDtoById(1L));
        assertEquals("Nie znaleziono doktora o podanym id", exception.getMessage());
        verify(doctorRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(doctorRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void addDoctor_DoctorNotFound_DoctorAddedAndReturned() {
        // given
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        Doctor doctor = doctorMapper.toEntity(createDoctorCommand);
        User user = new User(null, "Jan", "Kowalski");
        doctor.setUser(user);
        when(doctorRepository.findByEmail("email")).thenReturn(Optional.empty());
        when(userRepository.findByFirstNameAndLastName("Jan", "Kowalski")).thenReturn(Optional.empty());
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        // when
        DoctorDto result = service.addDoctor(createDoctorCommand);
        // then
        Assertions.assertAll(
                () -> assertEquals("email", result.email()),
                () -> assertEquals("Jan", result.user().firstName()),
                () -> assertEquals("Kowalski", result.user().lastName()),
                () -> assertEquals("123", doctor.getPassword()),
                () -> assertEquals(Specialization.DERMATOLOGIST, result.specialization())
        );
        verify(doctorRepository, times(1)).findByEmail("email");
        verify(userRepository, times(1)).findByFirstNameAndLastName("Jan", "Kowalski");
        verify(doctorRepository, times(1)).save(argThat(new DoctorArgumentMatcher(doctor)));
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
        assertEquals("Doktor z podanym emailem znajduje się już w bazie", exception.getMessage());
        verify(doctorRepository, times(1)).findByEmail(email);
        verifyNoMoreInteractions(doctorRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void addDoctor_UserExists_UserAlreadyExistsExceptionThrown() {
        // given
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        Doctor doctor = doctorMapper.toEntity(createDoctorCommand);
        User user = new User(null, createDoctorCommand. firstName(), createDoctorCommand.lastName());
        doctor.setUser(user);
        when(doctorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByFirstNameAndLastName(createDoctorCommand.firstName(), createDoctorCommand.lastName())).thenReturn(Optional.of(user));
        // when & then
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> service.addDoctor(createDoctorCommand));
        assertEquals("Ta osoba została już dodana do systemu", exception.getMessage());
        verify(doctorRepository,times(1)).findByEmail("email");
        verify(userRepository, times(1)).findByFirstNameAndLastName("Jan", "Kowalski");
        verifyNoMoreInteractions(doctorRepository, userRepository);
    }

    @Test
    void updateDoctorById_doctorFound_DoctorUpdatedAndReturned() {
        // given
        Long doctorId = 1L;
        CreateDoctorCommand createDoctorCommand = new CreateDoctorCommand("email", "Jan", "Kowalski",
                "123", Specialization.DERMATOLOGIST);
        CreateDoctorCommand createDoctorCommandNewData = new CreateDoctorCommand("emailUpdated@onet.pl", "Jan", "Kowalski",
                "123", Specialization.DERMATOLOGIST);
        Doctor doctor = doctorMapper.toEntity(createDoctorCommand);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        // when
        DoctorDto result = service.updateDoctorById(1L, createDoctorCommandNewData);
        // then
        Assertions.assertAll(
                () -> assertEquals("emailUpdated@onet.pl", result.email()),
                () -> assertEquals("Jan", result.user().firstName()),
                () -> assertEquals("Kowalski", result.user().lastName()),
                () -> assertEquals("123", doctor.getPassword()),
                () -> assertEquals(Specialization.DERMATOLOGIST, result.specialization())
        );
        verify(doctorRepository, times(1)).findById(1L);
        verify(doctorRepository, times(1)).save(argThat(new DoctorArgumentMatcher(doctor)));
        verifyNoMoreInteractions(doctorRepository);
    }

    @Test
    void updateDoctorById_doctorNotFound_DoctorNotFoundExceptionThrown() {
        // given
        Long doctorId = 1L;
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());
        // when & then
        DoctorNotFoundException exception = assertThrows(DoctorNotFoundException.class, () -> service.updateDoctorById(doctorId, createDoctorCommand));
        assertEquals("Nie znaleziono doktora o podanym id", exception.getMessage());
        verify(doctorRepository, times(1)).findById(1L);
    }

    @Test
    void deleteDoctorById_DoctorFound_DoctorDeletedAndReturned() {
        // given
        Long doctorId = 1L;
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        Doctor doctor = doctorMapper.toEntity(createDoctorCommand);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        doNothing().when(doctorRepository).delete(doctor);
        // when
        DoctorDto result = service.deleteDoctorById(doctorId);
        // then
        Assertions.assertAll(
                () -> assertEquals("email", result.email()),
                () -> assertEquals("Jan", result.user().firstName()),
                () -> assertEquals("Kowalski", result.user().lastName()),
                () -> assertEquals("123", doctor.getPassword()),
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
        assertEquals("Nie znaleziono doktora o podanym id", exception.getMessage());
        verify(doctorRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(doctorRepository);
        verifyNoInteractions(userRepository);
    }

    private CreateDoctorCommand makeCreateDoctorCommand() {
        return new CreateDoctorCommand("email", "Jan", "Kowalski",
                "123", Specialization.DERMATOLOGIST);
    }

    private Doctor createDoctor() {
        CreateDoctorCommand createDoctorCommand = makeCreateDoctorCommand();
        return doctorMapper.toEntity(createDoctorCommand);
    }
}

