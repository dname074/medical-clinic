package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.exception.PatientAlreadyExistsException;
import com.dname074.medicalclinic.exception.PatientNotFoundException;
import com.dname074.medicalclinic.mapper.PatientMapper;
import com.dname074.medicalclinic.model.CreatePatientCommand;
import com.dname074.medicalclinic.model.ChangePasswordCommand;
import com.dname074.medicalclinic.model.PatientDto;
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
        return repository.getAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public PatientDto findPatientByEmail(String email) {
        return repository.findByEmail(email)
                .map(mapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException("Nie udało się znaleźć pacjenta"));
    }

    public PatientDto addPatient(CreatePatientCommand patient) {
        return repository.add(mapper.createPatientCommandToEntity(patient))
                .map(mapper::toDto)
                .orElseThrow(() -> new PatientAlreadyExistsException("Podany pacjent już istnieje w bazie"));
    }

    public PatientDto removePatient(String email) {
        return repository.remove(email)
                .map(mapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException("Nie udało się znaleźć pacjenta"));
    }

    public PatientDto updatePatient(String email, CreatePatientCommand updatedPatient) {
        return repository.update(email, mapper.createPatientCommandToEntity(updatedPatient))
                .map(mapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException("Nie udało się znaleźć pacjenta"));
    }

    public PatientDto modifyPatientPassword(String email, ChangePasswordCommand newPassword) {
        return repository.modifyPassword(email, mapper.changePasswordCommandToEntity(newPassword))
                .map(mapper::toDto)
                .orElseThrow(() -> new PatientNotFoundException("Nie udało się znaleźć pacjenta"));
    }
}
