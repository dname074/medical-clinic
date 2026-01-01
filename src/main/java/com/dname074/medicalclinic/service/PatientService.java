package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.mapper.PatientMapper;
import com.dname074.medicalclinic.model.CreatePatientCommand;
import com.dname074.medicalclinic.model.ChangePasswordCommand;
import com.dname074.medicalclinic.model.PatientDto;
import com.dname074.medicalclinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository repository;
    private final PatientMapper mapper;

    public List<PatientDto> findAll() {
        return repository.getAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public PatientDto addPatient(CreatePatientCommand patient) {
        return mapper.toDto(repository.add(mapper.createPatientCommandToEntity(patient)));
    }

    public Optional<PatientDto> findPatientByEmail(String email) {
        return repository.findByEmail(email)
                .map(mapper::toDto);
    }

    public Optional<PatientDto> removePatient(String email) {
        return repository.remove(email)
                .map(mapper::toDto);
    }

    public Optional<PatientDto> updatePatient(String email, CreatePatientCommand updatedPatient) {
        return repository.update(email, mapper.createPatientCommandToEntity(updatedPatient))
                .map(mapper::toDto);
    }

    public Optional<PatientDto> modifyPatientPassword(String email, ChangePasswordCommand newPassword) {
        return repository.modifyPassword(email, mapper.changePasswordCommandToEntity(newPassword))
                .map(mapper::toDto);
    }
}
