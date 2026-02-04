package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.dto.VisitDto;
import com.dname074.medicalclinic.dto.command.CreateVisitCommand;
import com.dname074.medicalclinic.exception.doctor.DoctorNotFoundException;
import com.dname074.medicalclinic.exception.patient.PatientNotFoundException;
import com.dname074.medicalclinic.exception.visit.VisitAlreadyTakenException;
import com.dname074.medicalclinic.exception.visit.VisitExpiredException;
import com.dname074.medicalclinic.exception.visit.VisitNotFoundException;
import com.dname074.medicalclinic.mapper.VisitMapper;
import com.dname074.medicalclinic.model.Doctor;
import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.model.Visit;
import com.dname074.medicalclinic.repository.DoctorRepository;
import com.dname074.medicalclinic.repository.PatientRepository;
import com.dname074.medicalclinic.repository.VisitRepository;
import com.dname074.medicalclinic.validation.VisitValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final VisitMapper visitMapper;
    private final VisitValidator validator;

    public Page<VisitDto> getVisitsByPatientId(Long id, Pageable pageRequest) {
        return visitRepository.findByPatientId(id, pageRequest)
                .map(visitMapper::toDto);
    }

    @Transactional
    public VisitDto addAvailableVisit(CreateVisitCommand createVisitCommand) {
        validator.validateVisitDate(createVisitCommand.startDate(), createVisitCommand.endDate());
        Doctor doctor = doctorRepository.findById(createVisitCommand.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Nie znaleziono doktora o podanym id"));
        Visit visit = visitMapper.toEntity(createVisitCommand);
        visit.setDoctor(doctor);
        doctor.addVisit(visit);
        return visitMapper.toDto(visitRepository.save(visit));
    }

    @Transactional
    public VisitDto assign(Long visitId, Long patientId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("Nie znaleziono terminu wizyty o podanym id"));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Nie znaleziono pacjenta o podanym id"));
        if (visit.getPatient() != null) {
            throw new VisitAlreadyTakenException("Ten termin wizyty jest już zajęty");
        }
        if (visit.getStartDate().isBefore(LocalDateTime.now())) {
            throw new VisitExpiredException("Ten termin wizyty poprzedza aktualną datę i nie jest już dostępny");
        }
        visit.setPatient(patient);
        patient.addVisit(visit);
        return visitMapper.toDto(visitRepository.save(visit));
    }
}
