package pl.javakurs.dname074.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import pl.javakurs.dname074.model.Doctor;
import pl.javakurs.dname074.model.Specialization;

public interface DoctorServiceProvider {
    Page<Doctor> findAllDoctors(Pageable pageRequest, Specialization specialization);

    Doctor getDoctorDtoById(Long id, Authentication auth);

    Doctor addDoctor(CreateDoctorCommand createDoctorCommand);

    Doctor updateDoctorById(Long doctorId, CreateDoctorCommand createDoctorCommand, Authentication auth);

    Doctor deleteDoctorById(Long doctorId);
}
