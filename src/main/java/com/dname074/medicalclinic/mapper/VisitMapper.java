package com.dname074.medicalclinic.mapper;

import com.dname074.medicalclinic.dto.VisitDto;
import com.dname074.medicalclinic.dto.command.CreateVisitCommand;
import com.dname074.medicalclinic.model.Visit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VisitMapper {
    Visit toEntity(CreateVisitCommand createVisitCommand);
    VisitDto toDto(Visit visit);
}
