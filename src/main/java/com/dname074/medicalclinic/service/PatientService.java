package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.exception.patient.PatientAlreadyExistsException;
import com.dname074.medicalclinic.exception.patient.PatientNotFoundException;
import com.dname074.medicalclinic.mapper.PatientMapper;
import com.dname074.medicalclinic.dto.command.CreatePatientCommand;
import com.dname074.medicalclinic.dto.command.ChangePasswordCommand;
import com.dname074.medicalclinic.dto.PatientDto;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.model.User;
import com.dname074.medicalclinic.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository repository;
    private final PatientMapper mapper;

    public Page<PatientDto> findAll(Pageable pageRequest) {
        return repository.findAllWithUsers(pageRequest)
                .map(mapper::toDto);
    }

    public PatientDto getPatientDtoById(Long patientId) {
        Patient patient = getPatientById(patientId);
        return mapper.toDto(patient);
    }

    @Transactional
    public PatientDto addPatient(CreatePatientCommand createPatientCommand) {
        if (repository.findByEmail(createPatientCommand.email()).isPresent()) {
            throw new PatientAlreadyExistsException("Pacjent o podanym adresie email już istnieje w bazie danych");
        }
        User user = new User(null, createPatientCommand.firstName(), createPatientCommand.lastName());
        Patient patient = mapper.toEntity(createPatientCommand);
        patient.setUser(user);
        return mapper.toDto(repository.save(patient));
    }

    @Transactional
    public PatientDto deletePatientById(Long patientId) {
        Patient patient = getPatientById(patientId);
        repository.delete(patient);
        return mapper.toDto(patient);
    }

    @Transactional
    public PatientDto updatePatientById(Long patientId, CreatePatientCommand createPatientCommand) {
        Patient patient = repository.findById(patientId)
                        .orElseThrow(() -> new PatientNotFoundException("Nie znaleziono pacjenta o podanym id"));
        patient.update(createPatientCommand);
        return mapper.toDto(repository.save(patient));
    }

    @Transactional
    public PatientDto modifyPatientPasswordById(Long patientId, ChangePasswordCommand newPassword) {
        Patient patient = getPatientById(patientId);
        patient.setPassword(mapper.changePasswordCommandToEntity(newPassword));
        return mapper.toDto(patient);
    }

    private Patient getPatientById(Long patientId) {
        return repository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Nie udało się znaleźć pacjenta o podanym emailu"));
    }
}
