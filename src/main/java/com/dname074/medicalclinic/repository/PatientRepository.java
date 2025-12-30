package com.dname074.medicalclinic.repository;

import com.dname074.medicalclinic.model.ChangePasswordCommand;
import com.dname074.medicalclinic.model.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PatientRepository {
    private final List<Patient> patients;

    public List<Patient> getAll() {
        return new ArrayList<>(patients);
    }

    public Patient add(Patient patient) {
        patients.add(patient);
        return patients.getLast();
    }

    public Optional<Patient> getByEmail(String email) {
        return patients.stream()
                .filter(patient -> patient.getEmail().equals(email))
                .findFirst();
    }

    public Optional<Patient> remove(String email) {
        Optional<Patient> removedPatient = getByEmail(email);
        patients.removeIf(patient -> patient.getEmail().equals(email));
        return removedPatient;
    }

    public Optional<Patient> update(String email, Patient updatedPatient) {
        Optional<Patient> foundPatient = getByEmail(email);
        foundPatient.ifPresent(patient -> patient.update(updatedPatient));
        return foundPatient;
    }

    public Optional<Patient> modifyPassword(String email, ChangePasswordCommand newPassword) {
        Optional<Patient> foundPatient = getByEmail(email);
        foundPatient.ifPresent(patient -> patient.setPassword(newPassword.password()));
        return foundPatient;
    }
}
