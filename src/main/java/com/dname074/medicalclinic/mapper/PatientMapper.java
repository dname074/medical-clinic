package com.dname074.medicalclinic.mapper;

import com.dname074.medicalclinic.dto.ChangePasswordCommand;
import com.dname074.medicalclinic.dto.CreatePatientCommand;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.dto.PatientDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.firstName", source = "firstName")
    Patient createPatientCommandToEntity(CreatePatientCommand patientCommand);
    @Mapping(target = "surname", source = "user.lastName")
    @Mapping(target = "firstName", source = "user.firstName")
    PatientDto toDto(Patient patient);
    default String changePasswordCommandToEntity(ChangePasswordCommand passwordCommand) {
        if (passwordCommand == null) {
            return null;
        }
        return passwordCommand.password();
    }
}
