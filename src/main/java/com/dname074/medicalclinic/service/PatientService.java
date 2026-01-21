package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.exception.PatientAlreadyExistsException;
import com.dname074.medicalclinic.exception.PatientNotFoundException;
import com.dname074.medicalclinic.mapper.PatientMapper;
import com.dname074.medicalclinic.dto.CreatePatientCommand;
import com.dname074.medicalclinic.dto.ChangePasswordCommand;
import com.dname074.medicalclinic.dto.PatientDto;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.model.User;
import com.dname074.medicalclinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository repository;
    private final PatientMapper mapper;

    public List<PatientDto> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public PatientDto getPatientDtoByEmail(String email) {
        Patient patient = getPatientByEmail(email);
        return mapper.toDto(patient);
    }

    public PatientDto addPatient(CreatePatientCommand createPatientCommand) {
        if (repository.findByEmail(createPatientCommand.email()).isPresent()) {
            throw new PatientAlreadyExistsException("Pacjent o podanym adresie email już istnieje w bazie danych");
        }
        User user = new User(null, createPatientCommand.firstName(), createPatientCommand.lastName(), null, null);
        Patient patient = mapper.createPatientCommandToEntity(createPatientCommand);
        patient.setUser(user);
        return mapper.toDto(repository.save(patient));
    }

    public PatientDto removePatient(String email) {
        Patient patient = getPatientByEmail(email);
        repository.delete(patient);
        return mapper.toDto(patient);
    }

    public PatientDto updatePatient(String email, CreatePatientCommand createPatientCommand) {
        Patient patient = getPatientByEmail(email);
        patient.update(createPatientCommand);
        repository.save(patient);
        return mapper.toDto(patient);
    }

    public PatientDto modifyPatientPassword(String email, ChangePasswordCommand newPassword) {
        Patient patient = getPatientByEmail(email);
        patient.setPassword(mapper.changePasswordCommandToEntity(newPassword));
        return mapper.toDto(patient);
    }

    private Patient getPatientByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException("Nie udało się znaleźć pacjenta o podanym emailu"));
    }
}
