package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.dto.CreateInstitutionCommand;
import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.dto.InstitutionDto;
import com.dname074.medicalclinic.exception.DoctorNotFoundException;
import com.dname074.medicalclinic.exception.InstitutionExistsException;
import com.dname074.medicalclinic.exception.InstitutionNotFoundException;
import com.dname074.medicalclinic.mapper.DoctorMapper;
import com.dname074.medicalclinic.mapper.InstitutionMapper;
import com.dname074.medicalclinic.model.Doctor;
import com.dname074.medicalclinic.model.Institution;
import com.dname074.medicalclinic.repository.DoctorRepository;
import com.dname074.medicalclinic.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstitutionService {
    private final InstitutionRepository institutionRepository;
    private final DoctorRepository doctorRepository;
    private final InstitutionMapper institutionMapper;
    private final DoctorMapper doctorMapper;

    public List<InstitutionDto> findAllInstitutions() {
        return institutionRepository.findAll().stream()
                .map(institutionMapper::toDto)
                .toList();
    }

    public InstitutionDto getInstitutionDtoById(Long id) {
        Institution institution = getInstitutionById(id);
        return institutionMapper.toDto(institution);
    }

    public InstitutionDto addInstitution(CreateInstitutionCommand createInstitutionCommand) {
        if (institutionRepository.findByName(createInstitutionCommand.name()).isPresent()) {
            throw new InstitutionExistsException("Podana placówka już istnieje w systemie");
        }
        Institution institution = institutionMapper.toEntity(createInstitutionCommand);
        institutionRepository.save(institution);
        return institutionMapper.toDto(institution);
    }

    public InstitutionDto updateInstitution(CreateInstitutionCommand createInstitutionCommand, Long institutionId) {
        Institution institution = getInstitutionById(institutionId);
        institution.update(createInstitutionCommand);
        institutionRepository.save(institution);
        return institutionMapper.toDto(institution);
    }

    public DoctorDto assignDoctorToInstitution(Long doctorId, Long institutionId) {
        Institution institution = getInstitutionById(institutionId);
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Nie znaleziono doktora o podanym id"));
        institution.addDoctor(doctor);
        doctor.addInstitution(institution);
        institutionRepository.save(institution);
        doctorRepository.save(doctor);
        return doctorMapper.toDto(doctor);
    }

    public InstitutionDto deleteInstitutionById(Long institutionId) {
        Institution institution = getInstitutionById(institutionId);
        institutionRepository.delete(institution);
        return institutionMapper.toDto(institution);
    }

    public DoctorDto removeDoctorFromInstitution(Long institutionId, Long doctorId) {
        Institution institution = getInstitutionById(institutionId);
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Nie znaleziono doktora o podanym id"));
        institution.removeDoctor(doctor);
        doctorRepository.save(doctor);
        institutionRepository.save(institution);
        return doctorMapper.toDto(doctor);
    }

    private Institution getInstitutionById(Long id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new InstitutionNotFoundException("Nie znaleziono instytucji o podanym id"));
    }
}
