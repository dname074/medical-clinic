package pl.javakurs.dname074.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.javakurs.dname074.model.CreateDoctorCommand;
import pl.javakurs.dname074.model.Doctor;
import pl.javakurs.dname074.model.Page;
import pl.javakurs.dname074.model.Role;
import pl.javakurs.dname074.model.Specialization;
import pl.javakurs.dname074.model.exception.doctor.DoctorAlreadyExistsException;
import pl.javakurs.dname074.model.exception.doctor.DoctorNotFoundException;
import pl.javakurs.dname074.model.exception.user.UserAlreadyExistsException;

import java.nio.file.AccessDeniedException;

@Slf4j
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorServiceProvider {
    private final DoctorRepositoryProvider doctorRepository;
    private final UserRepositoryProvider userRepository;
    private final AuthorizationService authService;

    public Page<Doctor> findAllDoctors(Pageable pageRequest, Specialization specialization) {
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

    public Doctor getDoctorDtoById(Long id, Authentication auth) {
        log.info("Process of finding doctor by id started");
        Doctor doctor = getDoctorById(id);
        validateAccess(doctor, auth);
        log.info("Process of finding doctor by id ended");
        return doctorMapper.toDto(doctor);
    }

    @Transactional
    public DoctorDto addDoctor(Doctor createDoctorCommand) {
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
    public Doctor updateDoctorById(Long doctorId, CreateDoctorCommand createDoctorCommand, Authentication auth) {
        log.info("Process of updating existing doctor started");
        Doctor doctor = getDoctorById(doctorId);
        validateAccess(doctor, auth);
        doctor.update(createDoctorCommand);
        doctorRepository.save(doctor);
        log.info("Process of updating existing doctor ended");
        return doctorMapper.toDto(doctor);
    }

    @Transactional
    public Doctor deleteDoctorById(Long doctorId) {
        log.info("Process of deleting doctor started");
        Doctor doctor = getDoctorById(doctorId);
        doctorRepository.delete(doctor);
        log.info("Process of deleting doctor ended");
        return doctorMapper.toDto(doctor);
    }

    private void validateAddingDoctor(Doctor doctor) {
        if (doctorRepository.findByEmail(doctor.getEmail()).isPresent()) {
            throw new DoctorAlreadyExistsException("Doctor with provided email already exists");
        }
        userRepository.findByFirstNameAndLastName(doctor.getUser().getFirstName(), doctor.getUser().getLastName())
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
