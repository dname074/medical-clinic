package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Patient findPatientByEmail(String email) {
        return repository.getByEmail(email);
    }

    public Patient removePatient(String email) {
        return repository.remove(email);
    }

    public Patient updatePatient(String email, Patient updatedPatient) {
        return repository.update(email, updatedPatient);
    }

    public List<Patient> findPatientsByParameters(String firstName,
                                                  String lastName) {
        return repository.getAll().stream()
                .filter(patient -> firstName == null || patient.getFirstName().equals(firstName))
                .filter(patient -> lastName == null || patient.getLastName().equals(lastName))
                .toList();
    }
}
