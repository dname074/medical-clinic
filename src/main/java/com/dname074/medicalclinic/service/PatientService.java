package com.dname074.medicalclinic.service;

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

    public List<PatientDto> findAll() {
        return repository.getAll();
    }

    public PatientDto addPatient(CreatePatientCommand patient) {
        return repository.add(patient);
    }

    public Optional<PatientDto> findPatientByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Optional<PatientDto> removePatient(String email) {
        return repository.remove(email);
    }

    public Optional<PatientDto> updatePatient(String email, CreatePatientCommand updatedPatient) {
        return repository.update(email, updatedPatient);
    }

    public Optional<PatientDto> modifyPatientPassword(String email, ChangePasswordCommand newPassword) {
        return repository.modifyPassword(email, newPassword);
    }
}
