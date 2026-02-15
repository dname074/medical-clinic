package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.command.CreateInstitutionCommand;
import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.dto.InstitutionDto;
import com.dname074.medicalclinic.exception.doctor.DoctorNotFoundException;
import com.dname074.medicalclinic.exception.institution.InstitutionExistsException;
import com.dname074.medicalclinic.exception.institution.InstitutionNotFoundException;
import com.dname074.medicalclinic.mapper.DoctorMapper;
import com.dname074.medicalclinic.mapper.InstitutionMapper;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.model.Doctor;
import com.dname074.medicalclinic.model.Institution;
import com.dname074.medicalclinic.repository.DoctorRepository;
import com.dname074.medicalclinic.repository.InstitutionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InstitutionService {
    private final InstitutionRepository institutionRepository;
    private final DoctorRepository doctorRepository;
    private final InstitutionMapper institutionMapper;
    private final DoctorMapper doctorMapper;
    private final PageMapper pageMapper;

    public PageDto<InstitutionDto> findAllInstitutions(Pageable pageRequest) {
        return pageMapper.toInstitutionDto(institutionRepository.findAllWithDoctors(pageRequest)
                .map(institutionMapper::toDto));
    }

    public InstitutionDto getInstitutionDtoById(Long institutionId) {
        Institution institution = getInstitutionById(institutionId);
        return institutionMapper.toDto(institution);
    }

    @Transactional
    public InstitutionDto addInstitution(CreateInstitutionCommand createInstitutionCommand) {
        if (institutionRepository.findByName(createInstitutionCommand.name()).isPresent()) {
            throw new InstitutionExistsException("Podana placówka już istnieje w systemie");
        }
        Institution institution = institutionMapper.toEntity(createInstitutionCommand);
        institutionRepository.save(institution);
        return institutionMapper.toDto(institution);
    }

    @Transactional
    public InstitutionDto updateInstitution(CreateInstitutionCommand createInstitutionCommand, Long institutionId) {
        Institution institution = getInstitutionById(institutionId);
        institution.update(createInstitutionCommand);
        institutionRepository.save(institution);
        return institutionMapper.toDto(institution);
    }

    @Transactional
    public DoctorDto assignDoctorToInstitution(Long doctorId, Long institutionId) {
        Institution institution = getInstitutionById(institutionId);
        Doctor doctor = getDoctorById(doctorId);
        institution.addDoctor(doctor);
        doctor.addInstitution(institution);
        institutionRepository.save(institution);
        doctorRepository.save(doctor);
        return doctorMapper.toDto(doctor);
    }

    @Transactional
    public InstitutionDto deleteInstitutionById(Long institutionId) {
        Institution institution = getInstitutionById(institutionId);
        institutionRepository.delete(institution);
        return institutionMapper.toDto(institution);
    }

    @Transactional
    public DoctorDto removeDoctorFromInstitution(Long institutionId, Long doctorId) {
        Institution institution = getInstitutionById(institutionId);
        Doctor doctor = getDoctorById(doctorId);
        institution.removeDoctor(doctor);
        doctorRepository.save(doctor);
        institutionRepository.save(institution);
        return doctorMapper.toDto(doctor);
    }

    private Institution getInstitutionById(Long institutionId) {
        return institutionRepository.findById(institutionId)
                .orElseThrow(() -> new InstitutionNotFoundException("Nie znaleziono instytucji o podanym id"));
    }

    private Doctor getDoctorById(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Nie znaleziono doktora o podanym id"));
    }
}
