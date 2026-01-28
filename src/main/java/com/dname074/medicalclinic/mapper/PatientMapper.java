package com.dname074.medicalclinic.mapper;

import com.dname074.medicalclinic.dto.command.ChangePasswordCommand;
import com.dname074.medicalclinic.dto.command.CreatePatientCommand;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.dto.PatientDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    Patient createPatientCommandToEntity(CreatePatientCommand patientCommand);
    PatientDto toDto(Patient patient);
    default String changePasswordCommandToEntity(ChangePasswordCommand passwordCommand) {
        if (passwordCommand == null) {
            return null;
        }
        return passwordCommand.password();
    }
}
