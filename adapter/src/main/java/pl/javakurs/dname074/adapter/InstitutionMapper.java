package pl.javakurs.dname074.adapter;

import pl.javakurs.dname074.dto.CreateInstitutionCommand;
import com.dname074.medicalclinic.dto.InstitutionDto;
import com.dname074.medicalclinic.model.Institution;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InstitutionMapper {
    Institution toEntity(CreateInstitutionCommand createInstitutionCommand);
    InstitutionDto toDto(Institution institution);
}
