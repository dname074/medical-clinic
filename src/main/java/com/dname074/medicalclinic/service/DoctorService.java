package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.authorization.AuthorizationService;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.command.CreateDoctorCommand;
import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.exception.doctor.DoctorAlreadyExistsException;
import com.dname074.medicalclinic.exception.doctor.DoctorNotFoundException;
import com.dname074.medicalclinic.exception.user.UserAlreadyExistsException;
import com.dname074.medicalclinic.mapper.DoctorMapper;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.model.Doctor;
import com.dname074.medicalclinic.model.Role;
import com.dname074.medicalclinic.model.Specialization;
import com.dname074.medicalclinic.model.User;
import com.dname074.medicalclinic.repository.DoctorRepository;
import com.dname074.medicalclinic.repository.UserRepository;
import com.dname074.medicalclinic.specification.DoctorSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DoctorMapper doctorMapper;
    private final PageMapper pageMapper;
    private final AuthorizationService authService;

    public PageDto<DoctorDto> findAllDoctors(Pageable pageRequest, Specialization specialization) {
        log.info("Process of finding all doctors started");
        Specification<Doctor> filters = null;
        if (specialization != null) {
            filters = DoctorSpecifications.hasSpecialization(specialization);
        }
        PageDto<DoctorDto> page = pageMapper.toDoctorDto(doctorRepository.findAll(filters, pageRequest)
                .map(doctorMapper::toDto));
        log.info("Process of finding all doctors ended");
        return page;
    }

    public DoctorDto getDoctorDtoById(Long id, Authentication auth) {
        log.info("Process of finding doctor by id started");
        Doctor doctor = getDoctorById(id);
        validateAccess(doctor, auth);
        log.info("Process of finding doctor by id ended");
        return doctorMapper.toDto(doctor);
    }

    @Transactional
    public DoctorDto addDoctor(CreateDoctorCommand createDoctorCommand) {
        log.info("Process of adding new doctor started");
        validateAddingDoctor(createDoctorCommand);
        User user = new User(null, createDoctorCommand.keycloakId(), createDoctorCommand.firstName(), createDoctorCommand.lastName());
        Doctor doctor = doctorMapper.toEntity(createDoctorCommand);
        doctor.setUser(user);
        doctorRepository.save(doctor);
        log.info("Process of adding new doctor ended");
        return doctorMapper.toDto(doctor);
    }

    @Transactional
    public DoctorDto updateDoctorById(Long doctorId, CreateDoctorCommand createDoctorCommand, Authentication auth) {
        log.info("Process of updating existing doctor started");
        Doctor doctor = getDoctorById(doctorId);
        validateAccess(doctor, auth);
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

    private void validateAddingDoctor(CreateDoctorCommand createDoctorCommand) {
        if (doctorRepository.findByEmail(createDoctorCommand.email()).isPresent()) {
            throw new DoctorAlreadyExistsException("Doctor with provided email already exists");
        }
        userRepository.findByFirstNameAndLastName(createDoctorCommand.firstName(), createDoctorCommand.lastName())
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException("This user has already been added before");
                });
    }

    private void validateAccess(Doctor doctor, Authentication auth) {
        Jwt jwt = authService.extractJwt(auth);
        if (!authService.hasRole(auth, Role.ADMIN) &&
                !doctor.getUser().getKeycloakId().equals(jwt.getClaimAsString("sub"))) {
            throw new AccessDeniedException("Only doctors (or admins) can view/update their own profiles");
        }
    }

    private Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
    }
}
