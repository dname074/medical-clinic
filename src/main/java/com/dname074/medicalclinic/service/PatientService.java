package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.exception.PatientNotFoundException;
import com.dname074.medicalclinic.mapper.PatientMapper;
import com.dname074.medicalclinic.dto.CreatePatientCommand;
import com.dname074.medicalclinic.dto.ChangePasswordCommand;
import com.dname074.medicalclinic.dto.PatientDto;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository repository;
    private final PatientMapper mapper;

    public List<PatientDto> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public PatientDto findPatientByEmail(String email) {
        return repository.findByEmail(email)
                .map(mapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException("Nie udało się znaleźć pacjenta"));
    }

    public PatientDto addPatient(CreatePatientCommand patient) {
        return mapper.toDto(repository.save(mapper.createPatientCommandToEntity(patient)));
    }

    public PatientDto removePatient(String email) {
        Patient patient = repository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException("Nie udało się znaleźć pacjenta"));

        repository.delete(patient);
        return mapper.toDto(patient);
    }

    public PatientDto updatePatient(String email, CreatePatientCommand updatedPatient) {
        Patient patient = repository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException("Nie udało się znaleźć pacjenta"));

        Patient newPatient = mapper.createPatientCommandToEntity(updatedPatient);
        patient.update(newPatient);
        repository.save(patient);
        return mapper.toDto(patient);
    }

    public PatientDto modifyPatientPassword(String email, ChangePasswordCommand newPassword) {
        Patient patient = repository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException("Nie udało się znaleźć pacjenta"));
        patient.setPassword(mapper.changePasswordCommandToEntity(newPassword));
        return mapper.toDto(patient);
    }
}
