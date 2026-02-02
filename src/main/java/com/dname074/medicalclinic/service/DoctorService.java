package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.dto.command.CreateDoctorCommand;
import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.exception.DoctorAlreadyExistsException;
import com.dname074.medicalclinic.exception.DoctorNotFoundException;
import com.dname074.medicalclinic.mapper.DoctorMapper;
import com.dname074.medicalclinic.model.Doctor;
import com.dname074.medicalclinic.model.User;
import com.dname074.medicalclinic.repository.DoctorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final DoctorMapper mapper;

    public Page<DoctorDto> findAllDoctors(Pageable pageRequest) {
        return doctorRepository.findAllWithUsers(pageRequest)
                .map(mapper::toDto);
    }

    public DoctorDto getDoctorDtoById(Long id) {
        Doctor doctor = getDoctorById(id);
        return mapper.toDto(doctor);
    }

    @Transactional
    public DoctorDto addDoctor(CreateDoctorCommand createDoctorCommand) {
        if (doctorRepository.findByEmail(createDoctorCommand.email()).isPresent()) {
            throw new DoctorAlreadyExistsException("Doktor z podanym emailem znajduje się już w bazie");
        }
        User user = new User(null, createDoctorCommand.firstName(), createDoctorCommand.lastName());
        Doctor doctor = mapper.toEntity(createDoctorCommand);
        doctor.setUser(user);
        doctorRepository.save(doctor);
        return mapper.toDto(doctor);
    }

    @Transactional
    public DoctorDto updateDoctorById(Long doctorId, CreateDoctorCommand createDoctorCommand) {
        Doctor doctor = getDoctorById(doctorId);
        doctor.update(createDoctorCommand);
        doctorRepository.save(doctor);
        return mapper.toDto(doctor);
    }

    @Transactional
    public DoctorDto deleteDoctorById(Long doctorId) {
        Doctor doctor = getDoctorById(doctorId);
        doctorRepository.delete(doctor);
        return mapper.toDto(doctor);
    }

    private Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Nie znaleziono doktora o podanym id"));
    }
}
