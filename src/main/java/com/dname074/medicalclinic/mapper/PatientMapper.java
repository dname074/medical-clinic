package com.dname074.medicalclinic.mapper;

import com.dname074.medicalclinic.model.ChangePasswordCommand;
import com.dname074.medicalclinic.model.CreatePatientCommand;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.model.PatientDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    Patient createPatientCommandToEntity(CreatePatientCommand patientCommand);
    String changePasswordCommandToEntity(ChangePasswordCommand passwordCommand);
    PatientDto toDto(Patient patient);
}
