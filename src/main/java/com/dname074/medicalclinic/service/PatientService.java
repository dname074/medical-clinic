package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.model.ChangePasswordCommand;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository repository;

    public List<Patient> findAll() {
        return repository.getAll();
    }

    public Patient addPatient(Patient patient) {
        return repository.add(patient);
    }

    public Optional<Patient> findPatientByEmail(String email) {
        return repository.getByEmail(email);
    }

    public Optional<Patient> removePatient(String email) {
        return repository.remove(email);
    }

    public Optional<Patient> updatePatient(String email, Patient updatedPatient) {
        return repository.update(email, updatedPatient);
    }

    public Optional<Patient> modifyPatientPassword(String email, ChangePasswordCommand newPassword) {
        return repository.modifyPassword(email, newPassword);
    }
}
