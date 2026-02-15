package com.dname074.medicalclinic.argumentmatcher;

import com.dname074.medicalclinic.model.Doctor;
import lombok.RequiredArgsConstructor;
import org.mockito.ArgumentMatcher;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class DoctorArgumentMatcher implements ArgumentMatcher<Doctor> {
    private final Doctor doctor;

    @Override
    public boolean matches(Doctor newDoctor) {
        return nonNull(newDoctor) &&
                newDoctor.getEmail().equalsIgnoreCase(this.doctor.getEmail()) &&
                newDoctor.getUser().getFirstName().equalsIgnoreCase(this.doctor.getUser().getFirstName()) &&
                newDoctor.getUser().getLastName().equalsIgnoreCase(this.doctor.getUser().getLastName()) &&
                newDoctor.getPassword().equalsIgnoreCase(this.doctor.getPassword()) &&
                newDoctor.getSpecialization().equals(this.doctor.getSpecialization());
    }
}
