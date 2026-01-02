package com.dname074.medicalclinic.mapper;

import com.dname074.medicalclinic.model.ChangePasswordCommand;
import com.dname074.medicalclinic.model.CreatePatientCommand;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.model.PatientDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    Patient createPatientCommandToEntity(CreatePatientCommand patientCommand);
    default String changePasswordCommandToEntity(ChangePasswordCommand passwordCommand) {
        if (passwordCommand == null) {
            return null;
        }
        return passwordCommand.password();
    }
    @Mapping(target = "surname", source = "patient.lastName")
    PatientDto toDto(Patient patient);
}
