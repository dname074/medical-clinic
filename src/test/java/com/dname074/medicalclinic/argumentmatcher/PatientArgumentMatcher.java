package com.dname074.medicalclinic.argumentmatcher;


import com.dname074.medicalclinic.model.Patient;
import lombok.RequiredArgsConstructor;
import org.mockito.ArgumentMatcher;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class PatientArgumentMatcher implements ArgumentMatcher<Patient> {
    private final Patient patient;

    @Override
    public boolean matches(Patient newPatient) {
        return nonNull(newPatient) &&
                newPatient.getEmail().equalsIgnoreCase(this.patient.getEmail()) &&
                newPatient.getUser().getFirstName().equalsIgnoreCase(this.patient.getUser().getFirstName()) &&
                newPatient.getUser().getLastName().equalsIgnoreCase(this.patient.getUser().getLastName()) &&
                newPatient.getPassword().equalsIgnoreCase(this.patient.getPassword()) &&
                newPatient.getIdCardNo().equals(this.patient.getIdCardNo()) &&
                newPatient.getPhoneNumber().equalsIgnoreCase(this.patient.getPhoneNumber()) &&
                newPatient.getBirthday().equals(this.patient.getBirthday());
    }
}
