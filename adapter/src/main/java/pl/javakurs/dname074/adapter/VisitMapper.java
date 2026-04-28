package pl.javakurs.dname074.adapter;

import com.dname074.medicalclinic.dto.VisitDto;
import pl.javakurs.dname074.dto.CreateVisitCommand;
import com.dname074.medicalclinic.model.Visit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VisitMapper {
    Visit toEntity(CreateVisitCommand createVisitCommand);
    VisitDto toDto(Visit visit);
}
