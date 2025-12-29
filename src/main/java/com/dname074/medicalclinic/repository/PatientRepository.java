package com.dname074.medicalclinic.repository;

import com.dname074.medicalclinic.model.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

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

    public Patient getByEmail(String email) {
        return patients.stream()
                .filter(patient -> patient.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new InvalidParameterException("Nie znaleziono pacjenta o podanym emailu"));
    }

    public Patient remove(String email) {
        Patient patient = getByEmail(email);
        patients.remove(patient);
        return patient;
    }

    public Patient update(String email, Patient updatedPatient) {
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getEmail().equals(email)) {
                patients.set(i, updatedPatient);
                return updatedPatient;
            }
        }
        throw new InvalidParameterException("Podano niepoprawny email");
    }
}
