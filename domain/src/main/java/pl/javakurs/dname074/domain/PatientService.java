package pl.javakurs.dname074.domain;

import com.dname074.medicalclinic.authorization.AuthorizationService;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.exception.patient.PatientAlreadyExistsException;
import com.dname074.medicalclinic.exception.patient.PatientNotFoundException;
import com.dname074.medicalclinic.exception.user.UserAlreadyExistsException;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.mapper.PatientMapper;
import pl.javakurs.dname074.dto.command.CreatePatientCommand;
import pl.javakurs.dname074.dto.command.ChangePasswordCommand;
import com.dname074.medicalclinic.dto.PatientDto;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.model.Role;
import com.dname074.medicalclinic.model.User;
import com.dname074.medicalclinic.repository.PatientRepository;
import com.dname074.medicalclinic.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientMapper mapper;
    private final PageMapper pageMapper;
    private final AuthorizationService authService;

    public PageDto<PatientDto> findAll(Pageable pageRequest) {
        log.info("Process of finding patients by parameters started");
        PageDto<PatientDto> page = pageMapper.toPatientDto(patientRepository.findAllWithUsers(pageRequest)
                .map(mapper::toDto));
        log.info("Process of finding patients by parameters ended");
        return page;
    }

    public PatientDto getPatientDtoById(Long patientId, Authentication auth) {
        log.info("Process of finding patient by id started");
        Patient patient = getPatientById(patientId);
        validateAccess(patient, auth);
        log.info("Process of finding patient by id ended");
        return mapper.toDto(patient);
    }

    @Transactional
    public PatientDto addPatient(CreatePatientCommand createPatientCommand) {
        log.info("Process of adding new patient started");
        validateAddingPatient(createPatientCommand);
        User user = new User(null, createPatientCommand.keycloakId(), createPatientCommand.firstName(), createPatientCommand.lastName());
        Patient patient = mapper.toEntity(createPatientCommand);
        patient.setUser(user);
        patientRepository.save(patient);
        log.info("Process of adding new patient ended");
        return mapper.toDto(patient);
    }

    @Transactional
    public PatientDto updatePatientById(Long patientId, CreatePatientCommand createPatientCommand, Authentication auth) {
        log.info("Process of updating patient started");
        Patient patient = getPatientById(patientId);
        validateAccess(patient, auth);
        patient.update(createPatientCommand);
        patientRepository.save(patient);
        log.info("Process of updating patient ended");
        return mapper.toDto(patient);
    }

    @Transactional
    public PatientDto deletePatientById(Long patientId) {
        log.info("Process of deleting patient started");
        Patient patient = getPatientById(patientId);
        patientRepository.delete(patient);
        log.info("Process of deleting patient ended");
        return mapper.toDto(patient);
    }

    @Transactional
    public PatientDto modifyPatientPasswordById(Long patientId, ChangePasswordCommand newPassword, Authentication auth) {
        log.info("Process of modifying patient's password started");
        Patient patient = getPatientById(patientId);
        validateAccess(patient, auth);
        patient.setPassword(mapper.changePasswordCommandToEntity(newPassword));
        patientRepository.save(patient);
        log.info("Process of modifying patient's password ended");
        return mapper.toDto(patient);
    }

    private void validateAccess(Patient patient, Authentication auth) {
        Jwt jwt = authService.extractJwt(auth);
        if (!authService.hasRole(auth, Role.ADMIN) && !patient.getUser().getKeycloakId().equals(jwt.getClaimAsString("sub"))) {
            throw new AccessDeniedException("Only patients (or admins) can view/update their own profiles");
        }
    }

    private void validateAddingPatient(CreatePatientCommand createPatientCommand) {
        if (patientRepository.findByEmail(createPatientCommand.email()).isPresent()) {
            throw new PatientAlreadyExistsException("Patient with provided email already exists");
        }
        userRepository.findByFirstNameAndLastName(createPatientCommand.firstName(), createPatientCommand.lastName())
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException("This user has already been added before");
                });
    }

    private Patient getPatientById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
    }
}
