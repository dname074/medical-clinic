package com.dname074.medicalclinic.mapper;

import com.dname074.medicalclinic.dto.command.CreateDoctorCommand;
import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.model.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorMapper {
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    Doctor toEntity(CreateDoctorCommand createDoctorCommand);
    DoctorDto toDto(Doctor doctor);
}
