package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.command.CreateDoctorCommand;
import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.exception.doctor.DoctorAlreadyExistsException;
import com.dname074.medicalclinic.exception.doctor.DoctorNotFoundException;
import com.dname074.medicalclinic.exception.user.UserAlreadyExistsException;
import com.dname074.medicalclinic.mapper.DoctorMapper;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.model.Doctor;
import com.dname074.medicalclinic.model.User;
import com.dname074.medicalclinic.repository.DoctorRepository;
import com.dname074.medicalclinic.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DoctorMapper doctorMapper;
    private final PageMapper pageMapper;

    public PageDto<DoctorDto> findAllDoctors(Pageable pageRequest) {
        log.info("Process of finding all doctors started");
        PageDto<DoctorDto> page = pageMapper.toDoctorDto(doctorRepository.findAllWithUsers(pageRequest)
                .map(doctorMapper::toDto));
        log.info("Process of finding all doctors ended");
        return page;
    }

    public DoctorDto getDoctorDtoById(Long id) {
        log.info("Process of finding doctor by id started");
        Doctor doctor = getDoctorById(id);
        log.info("Process of finding doctor by id ended");
        return doctorMapper.toDto(doctor);
    }

    @Transactional
    public DoctorDto addDoctor(CreateDoctorCommand createDoctorCommand) {
        log.info("Process of adding new doctor started");
        if (doctorRepository.findByEmail(createDoctorCommand.email()).isPresent()) {
            throw new DoctorAlreadyExistsException("Doktor z podanym emailem znajduje się już w bazie");
        }
        userRepository.findByFirstNameAndLastName(createDoctorCommand.firstName(), createDoctorCommand.lastName())
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException("Ta osoba została już dodana do systemu");
                });
        User user = new User(null, createDoctorCommand.firstName(), createDoctorCommand.lastName());
        Doctor doctor = doctorMapper.toEntity(createDoctorCommand);
        doctor.setUser(user);
        doctorRepository.save(doctor);
        log.info("Process of adding new doctor ended");
        return doctorMapper.toDto(doctor);
    }

    @Transactional
    public DoctorDto updateDoctorById(Long doctorId, CreateDoctorCommand createDoctorCommand) {
        log.info("Process of updating existing doctor started");
        Doctor doctor = getDoctorById(doctorId);
        doctor.update(createDoctorCommand);
        doctorRepository.save(doctor);
        log.info("Process of updating existing doctor ended");
        return doctorMapper.toDto(doctor);
    }

    @Transactional
    public DoctorDto deleteDoctorById(Long doctorId) {
        log.info("Process of deleting doctor started");
        Doctor doctor = getDoctorById(doctorId);
        doctorRepository.delete(doctor);
        log.info("Process of deleting doctor ended");
        return doctorMapper.toDto(doctor);
    }

    private Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Nie znaleziono doktora o podanym id"));
    }
}
