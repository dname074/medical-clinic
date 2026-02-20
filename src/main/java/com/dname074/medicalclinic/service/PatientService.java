package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.exception.patient.PatientAlreadyExistsException;
import com.dname074.medicalclinic.exception.patient.PatientNotFoundException;
import com.dname074.medicalclinic.exception.user.UserAlreadyExistsException;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.mapper.PatientMapper;
import com.dname074.medicalclinic.dto.command.CreatePatientCommand;
import com.dname074.medicalclinic.dto.command.ChangePasswordCommand;
import com.dname074.medicalclinic.dto.PatientDto;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.model.User;
import com.dname074.medicalclinic.repository.PatientRepository;
import com.dname074.medicalclinic.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientMapper mapper;
    private final PageMapper pageMapper;

    public PageDto<PatientDto> findAll(Pageable pageRequest) {
        log.info("Process of finding patients by parameters started");
        PageDto<PatientDto> page = pageMapper.toPatientDto(patientRepository.findAllWithUsers(pageRequest)
                .map(mapper::toDto));
        log.info("Process of finding patients by parameters ended");
        return page;
    }

    public PatientDto getPatientDtoById(Long patientId) {
        log.info("Process of finding patient by id started");
        Patient patient = getPatientById(patientId);
        log.info("Process of finding patient by id ended");
        return mapper.toDto(patient);
    }

    @Transactional
    public PatientDto addPatient(CreatePatientCommand createPatientCommand) {
        log.info("Process of adding new patient started");
        if (patientRepository.findByEmail(createPatientCommand.email()).isPresent()) {
            throw new PatientAlreadyExistsException("Pacjent o podanym adresie email już istnieje w bazie danych");
        }
        userRepository.findByFirstNameAndLastName(createPatientCommand.firstName(), createPatientCommand.lastName())
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException("Ta osoba została już dodana do systemu");
                });
        User user = new User(null, createPatientCommand.firstName(), createPatientCommand.lastName());
        Patient patient = mapper.toEntity(createPatientCommand);
        patient.setUser(user);
        patientRepository.save(patient);
        log.info("Process of adding new patient ended");
        return mapper.toDto(patient);
    }

    @Transactional
    public PatientDto updatePatientById(Long patientId, CreatePatientCommand createPatientCommand) {
        log.info("Process of updating patient started");
        Patient patient = getPatientById(patientId);
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
    public PatientDto modifyPatientPasswordById(Long patientId, ChangePasswordCommand newPassword) {
        log.info("Process of modifying patient's password started");
        Patient patient = getPatientById(patientId);
        patient.setPassword(mapper.changePasswordCommandToEntity(newPassword));
        patientRepository.save(patient);
        log.info("Process of modifying patient's password ended");
        return mapper.toDto(patient);
    }

    private Patient getPatientById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Nie udało się znaleźć pacjenta o podanym id"));
    }
}
