package com.dname074.medicalclinic.repository;

import com.dname074.medicalclinic.model.ChangePasswordCommand;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.model.PatientDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PatientRepository {
    private final List<Patient> patients;

    public List<PatientDto> getAll() {
        return patients.stream()
                .map(PatientDto::new)
                .toList();
    }

    public PatientDto add(Patient patient) {
        patients.add(patient);
        return new PatientDto(patients.getLast());
    }

    public Optional<PatientDto> findByEmail(String email) {
        return getByEmail(email)
                .map(PatientDto::new);
    }

    private Optional<Patient> getByEmail(String email) {
        return patients.stream()
                .filter(patient -> patient.getEmail().equals(email))
                .findFirst();
    }

    public Optional<PatientDto> remove(String email) {
        Optional<Patient> removedPatient = getByEmail(email);
        patients.removeIf(patient -> patient.getEmail().equals(email));
        return removedPatient
                .map(PatientDto::new);
    }

    public Optional<PatientDto> update(String email, Patient updatedPatient) {
        Optional<Patient> foundPatient = getByEmail(email);
        foundPatient.ifPresent(patient -> patient.update(updatedPatient));
        return foundPatient
                .map(PatientDto::new);
    }

    public Optional<PatientDto> modifyPassword(String email, ChangePasswordCommand newPassword) {
        Optional<Patient> foundPatient = getByEmail(email);
        foundPatient.ifPresent(patient -> patient.setPassword(newPassword.password()));
        return foundPatient
                .map(PatientDto::new);
    }
}
