package com.dname074.medicalclinic.mapper;

import com.dname074.medicalclinic.dto.CreateInstitutionCommand;
import com.dname074.medicalclinic.dto.InstitutionDto;
import com.dname074.medicalclinic.model.Institution;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InstitutionMapper {
    InstitutionDto toDto(Institution institution);
    Institution toEntity(CreateInstitutionCommand createInstitutionCommand);
}
