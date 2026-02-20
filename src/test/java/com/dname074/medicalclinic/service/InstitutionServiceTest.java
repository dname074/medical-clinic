package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.argumentmatcher.DoctorArgumentMatcher;
import com.dname074.medicalclinic.argumentmatcher.InstitutionArgumentMatcher;
import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.dto.InstitutionDto;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.command.CreateDoctorCommand;
import com.dname074.medicalclinic.dto.command.CreateInstitutionCommand;
import com.dname074.medicalclinic.exception.doctor.DoctorAlreadyExistsException;
import com.dname074.medicalclinic.exception.doctor.DoctorNotFoundException;
import com.dname074.medicalclinic.exception.institution.InstitutionExistsException;
import com.dname074.medicalclinic.exception.institution.InstitutionNotFoundException;
import com.dname074.medicalclinic.mapper.DoctorMapper;
import com.dname074.medicalclinic.mapper.InstitutionMapper;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.model.Doctor;
import com.dname074.medicalclinic.model.Institution;
import com.dname074.medicalclinic.model.Specialization;
import com.dname074.medicalclinic.repository.DoctorRepository;
import com.dname074.medicalclinic.repository.InstitutionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class InstitutionServiceTest {
    InstitutionRepository institutionRepository;
    DoctorRepository doctorRepository;
    InstitutionMapper institutionMapper;
    DoctorMapper doctorMapper;
    InstitutionService institutionService;
    PageMapper pageMapper;

    @BeforeEach
    void setup() {
        this.institutionRepository = Mockito.mock(InstitutionRepository.class);
        this.doctorRepository = Mockito.mock(DoctorRepository.class);
        this.institutionMapper = Mappers.getMapper(InstitutionMapper.class);
        this.doctorMapper = Mappers.getMapper(DoctorMapper.class);
        this.pageMapper = Mappers.getMapper(PageMapper.class);
        this.institutionService = new InstitutionService(institutionRepository, doctorRepository, institutionMapper, doctorMapper, pageMapper);
    }

    @Test
    void findAllInstitutions_RequestCorrect_InstitutionsReturned() {
        // given
        Institution institution = createInstitution();
        List<Institution> institutions = List.of(institution);
        Pageable pageable = PageRequest.of(0, 1);
        Page<Institution> page = new PageImpl<>(institutions, pageable, 1);
        when(institutionRepository.findAllWithDoctors(pageable)).thenReturn(page);
        // when
        PageDto<InstitutionDto> result = institutionService.findAllInstitutions(pageable);
        // then
        Assertions.assertAll(
                () -> assertEquals(1, result.totalElements()),
                () -> assertEquals(1, result.totalPages()),
                () -> assertFalse(result.content().isEmpty())
        );
        verify(institutionRepository, times(1)).findAllWithDoctors(pageable);
        verifyNoMoreInteractions(institutionRepository);
        verifyNoInteractions(doctorRepository);
    }
    @Test
    void getInstitutionDtoById_InstitutionFound_InstitutionReturned() {
        // given
        Long institutionId = 1L;
        Institution institution = createInstitution();
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        // when
        InstitutionDto result = institutionService.getInstitutionDtoById(institutionId);
        // then
        Assertions.assertAll(
                () -> assertEquals("Placówka", result.name()),
                () -> assertEquals("Krakow", result.town()),
                () -> assertEquals("5555", result.zipCode()),
                () -> assertEquals("Szybka", result.street()),
                () -> assertEquals(42, result.placeNo())
        );
        verify(institutionRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(institutionRepository);
        verifyNoInteractions(doctorRepository);
    }

    @Test
    void getInstitutionDtoById_InstitutionNotFound_InstitutionNotFoundExceptionThrown() {
        // given
        Long institutionId = 1L;
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.empty());
        // when & then
        InstitutionNotFoundException exception = assertThrows(InstitutionNotFoundException.class, () -> institutionService.getInstitutionDtoById(institutionId));
        assertEquals("Nie znaleziono instytucji o podanym id", exception.getMessage());
        verify(institutionRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(institutionRepository);
        verifyNoInteractions(doctorRepository);
    }

    @Test
    void addInstitution_InstitutionNotFound_InstitutionCreatedAndReturned() {
        // given
        CreateInstitutionCommand createInstitutionCommand = makeCreateInstitutionCommand();
        Institution institution = institutionMapper.toEntity(createInstitutionCommand);
        when(institutionRepository.findByName(institution.getName())).thenReturn(Optional.empty());
        when(institutionRepository.save(institution)).thenReturn(institution);
        // when
        InstitutionDto result = institutionService.addInstitution(createInstitutionCommand);
        // then
        Assertions.assertAll(
                () -> assertEquals("Placówka", result.name()),
                () -> assertEquals("Krakow", result.town()),
                () -> assertEquals("5555", result.zipCode()),
                () -> assertEquals("Szybka", result.street()),
                () -> assertEquals(42, result.placeNo())
        );
        verify(institutionRepository, times(1)).findByName("Placówka");
        verify(institutionRepository, times(1)).save(argThat(new InstitutionArgumentMatcher(institution)));
        verifyNoMoreInteractions(institutionRepository);
        verifyNoInteractions(doctorRepository);
    }

    @Test
    void addInstitution_InstitutionExists_InstitutionExistsExceptionThrown() {
        // given
        CreateInstitutionCommand createInstitutionCommand = makeCreateInstitutionCommand();
        Institution institution = institutionMapper.toEntity(createInstitutionCommand);
        when(institutionRepository.findByName(createInstitutionCommand.name())).thenReturn(Optional.of(institution));
        // when & then
        InstitutionExistsException exception = assertThrows(InstitutionExistsException.class, () -> institutionService.addInstitution(createInstitutionCommand));
        assertEquals("Podana placówka już istnieje w systemie", exception.getMessage());
        verify(institutionRepository, times(1)).findByName("Placówka");
        verifyNoMoreInteractions(institutionRepository);
        verifyNoInteractions(doctorRepository);
    }

    @Test
    void updateInstitution_InstitutionExists_InstitutionUpdatedAndReturned() {
        // given
        Long institutionId = 1L;
        CreateInstitutionCommand createInstitutionCommandNewData =
                new CreateInstitutionCommand("Placówka", "Krakow", "5555", "Szybka", 42);
        Institution institution = createInstitution();
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(institutionRepository.save(institution)).thenReturn(institution);
        // when
        InstitutionDto result = institutionService.updateInstitution(createInstitutionCommandNewData, 1L);
        // then
        Assertions.assertAll(
                () -> assertEquals("Placówka", result.name()),
                () -> assertEquals("Krakow", result.town()),
                () -> assertEquals("5555", result.zipCode()),
                () -> assertEquals("Szybka", result.street()),
                () -> assertEquals(42, result.placeNo())
        );
        verify(institutionRepository, times(1)).findById(1L);
        verify(institutionRepository, times(1)).save(argThat(new InstitutionArgumentMatcher(institution)));
        verifyNoMoreInteractions(institutionRepository);
        verifyNoInteractions(doctorRepository);
    }

    @Test
    void updateInstitution_InstitutionNotFound_InstitutionNotFoundExceptionThrown() {
        // given
        Long institutionId = 1L;
        CreateInstitutionCommand createInstitutionCommand = makeCreateInstitutionCommand();
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.empty());
        // when & then
        InstitutionNotFoundException exception = assertThrows(InstitutionNotFoundException.class, () -> institutionService.updateInstitution(createInstitutionCommand, institutionId));
        assertEquals("Nie znaleziono instytucji o podanym id", exception.getMessage());
        verify(institutionRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(institutionRepository);
        verifyNoInteractions(doctorRepository);
    }

    @Test
    void assignDoctorToInstitution_DoctorAndInstitutionFound_DoctorAssignedToInstitution() {
        // given
        Long doctorId = 1L;
        Doctor doctor = createDoctor();
        Long institutionId = 1L;
        Institution institution = createInstitution();
        institution.setDoctors(new ArrayList<>());
        doctor.setInstitutions(new ArrayList<>());
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(institutionRepository.save(institution)).thenReturn(institution);
        // when
        DoctorDto result = institutionService.assignDoctorToInstitution(doctorId, institutionId);
        // then
        // do poprawy
        Assertions.assertAll(
                () -> assertTrue(doctor.getInstitutions().contains(institution)),
                () -> assertTrue(institution.getDoctors().contains(doctor))
        );
        verify(doctorRepository, times(1)).findById(1L);
        verify(doctorRepository, times(1)).save(argThat(new DoctorArgumentMatcher(doctor)));
        verify(institutionRepository, times(1)).findById(1L);
        verify(institutionRepository, times(1)).save(argThat(new InstitutionArgumentMatcher(institution)));
        verifyNoMoreInteractions(institutionRepository, doctorRepository);
    }

    @Test
    void assignDoctorToInstitution_DoctorNotFound_DoctorNotFoundExceptionThrown() {
        // given
        Long doctorId = 1L;
        Long institutionId = 1L;
        Institution institution = createInstitution();
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());
        // when & then
        DoctorNotFoundException exception = assertThrows(DoctorNotFoundException.class, () -> institutionService.assignDoctorToInstitution(doctorId, institutionId));
        assertEquals("Nie znaleziono doktora o podanym id", exception.getMessage());
        verify(institutionRepository, times(1)).findById(institutionId);
        verify(doctorRepository, times(1)).findById(doctorId);
        verifyNoMoreInteractions(institutionRepository, doctorRepository);
    }

    @Test
    void assignDoctorToInstitution_InstitutionNotFound_InstitutionNotFoundExceptionThrown() {
        // given
        Long doctorId = 1L;
        Long institutionId = 1L;
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.empty());
        // when & then
        InstitutionNotFoundException exception = assertThrows(InstitutionNotFoundException.class, () -> institutionService.assignDoctorToInstitution(doctorId, institutionId));
        assertEquals("Nie znaleziono instytucji o podanym id", exception.getMessage());
        verify(institutionRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(institutionRepository);
        verifyNoInteractions(doctorRepository);
    }

    @Test
    void assignDoctorToInstitution_DoctorAlreadyAssigned_DoctorAlreadyExistsExceptionThrown() {
        // given
        Long doctorId = 1L;
        Doctor doctor = createDoctor();
        Long institutionId = 1L;
        Institution institution = createInstitution();
        institution.setDoctors(List.of(doctor));
        doctor.setInstitutions(new ArrayList<>());
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        // when & then
        DoctorAlreadyExistsException exception = assertThrows(DoctorAlreadyExistsException.class, () -> institutionService.assignDoctorToInstitution(doctorId, institutionId));
        assertEquals("Podany doktor należy już do tej placówki", exception.getMessage());
        verify(doctorRepository, times(1)).findById(1L);
        verify(institutionRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(doctorRepository, institutionRepository);
    }

    @Test
    void assignDoctorToInstitution_InstitutionAlreadyAddedToDoctorsList_InstitutionExistsExceptionThrown() {
        // given
        Long doctorId = 1L;
        Doctor doctor = createDoctor();
        Long institutionId = 1L;
        Institution institution = createInstitution();
        institution.setDoctors(new ArrayList<>());
        doctor.setInstitutions(List.of(institution));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        // when & then
        InstitutionExistsException exception = assertThrows(InstitutionExistsException.class, () -> institutionService.assignDoctorToInstitution(doctorId, institutionId));
        assertEquals("Ten doktor jest już przypisany do podanej placówki", exception.getMessage());
        verify(doctorRepository, times(1)).findById(1L);
        verify(institutionRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(doctorRepository, institutionRepository);
    }

    @Test
    void deleteInstitutionById_InstitutionFound_InstitutionDeletedAndReturned() {
        // given
        Long institutionId = 1L;
        Institution institution = createInstitution();
        when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
        doNothing().when(institutionRepository).delete(institution);
        // when
        InstitutionDto result = institutionService.deleteInstitutionById(institutionId);
        // then
        Assertions.assertAll(
                () -> assertEquals("Placówka", result.name()),
                () -> assertEquals("Krakow", result.town()),
                () -> assertEquals("5555", result.zipCode()),
                () -> assertEquals("Szybka", result.street()),
                () -> assertEquals(42, result.placeNo())
        );
        verify(institutionRepository, times(1)).findById(1L);
        verify(institutionRepository, times(1)).delete(institution);
        verifyNoMoreInteractions(institutionRepository);
        verifyNoInteractions(doctorRepository);
    }

    @Test
    void deleteInstitution_InstitutionNotFound_InstitutionNotFoundExceptionThrown() {
        // given
        Long institutionid = 1L;
        when(institutionRepository.findById(institutionid)).thenReturn(Optional.empty());
        // when & then
        InstitutionNotFoundException exception = assertThrows(InstitutionNotFoundException.class, () -> institutionService.deleteInstitutionById(institutionid));
        assertEquals("Nie znaleziono instytucji o podanym id", exception.getMessage());
        verify(institutionRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(institutionRepository);
        verifyNoInteractions(doctorRepository);
    }

    @Test
    void removeDoctorFromInstitution_DoctorAndInstitutionFound_DoctorRemovedFromInstitutionAndReturned() {
        // todo
    }

    private CreateInstitutionCommand makeCreateInstitutionCommand() {
        return new CreateInstitutionCommand("Placówka", "Krakow", "5555", "Szybka", 42);
    }

    private Institution createInstitution() {
        return institutionMapper.toEntity(makeCreateInstitutionCommand());
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
