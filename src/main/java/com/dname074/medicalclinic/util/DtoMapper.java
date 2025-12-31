package com.dname074.medicalclinic.util;

import com.dname074.medicalclinic.model.CreatePatientCommand;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.model.PatientDto;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {
    public Patient createPatientDtotoPatient(CreatePatientCommand patientDto) {
        return new Patient(patientDto.getEmail(), patientDto.getPassword(),
                patientDto.getIdCardNo(), patientDto.getFirstName(), patientDto.getLastName(),
                patientDto.getPhoneNumber(), patientDto.getBirthday());
    }

    public PatientDto toDto(Patient patient) {
        return new PatientDto(patient.getEmail(), patient.getIdCardNo(), patient.getFirstName(),
                patient.getLastName(), patient.getPhoneNumber(), patient.getBirthday());
    }
}
