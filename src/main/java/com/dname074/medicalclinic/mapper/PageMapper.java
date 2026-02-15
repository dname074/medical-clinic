package com.dname074.medicalclinic.mapper;

import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.dto.InstitutionDto;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.PatientDto;
import com.dname074.medicalclinic.dto.VisitDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface PageMapper {
    @Mapping(target = "pageNumber", source = "page.pageable.pageNumber")
    @Mapping(target = "pageSize", source = "page.pageable.pageSize")
    PageDto<PatientDto> toPatientDto(Page<PatientDto> page);

    @Mapping(target = "pageNumber", source = "page.pageable.pageNumber")
    @Mapping(target = "pageSize", source = "page.pageable.pageSize")
    PageDto<DoctorDto> toDoctorDto(Page<DoctorDto> page);

    @Mapping(target = "pageNumber", source = "page.pageable.pageNumber")
    @Mapping(target = "pageSize", source = "page.pageable.pageSize")
    PageDto<InstitutionDto> toInstitutionDto(Page<InstitutionDto> page);

    @Mapping(target = "pageNumber", source = "page.pageable.pageNumber")
    @Mapping(target = "pageSize", source = "page.pageable.pageSize")
    PageDto<VisitDto> toVisitDto(Page<VisitDto> page);
}
