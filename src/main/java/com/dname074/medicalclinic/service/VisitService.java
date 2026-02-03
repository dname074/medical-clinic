package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.dto.VisitDto;
import com.dname074.medicalclinic.dto.command.CreateVisitCommand;
import com.dname074.medicalclinic.exception.doctor.DoctorNotFoundException;
import com.dname074.medicalclinic.exception.visit.InvalidVisitException;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final VisitMapper visitMapper;

    @Transactional
    public VisitDto addAvailableVisit(CreateVisitCommand createVisitCommand) {
        validateVisitDate(createVisitCommand.startDate(), createVisitCommand.endDate());
        Doctor doctor = doctorRepository.findById(createVisitCommand.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Nie znaleziono doktora o podanym id"));
        Visit visit = visitMapper.toEntity(createVisitCommand);
        visit.setDoctor(doctor);
        doctor.addVisit(visit);
        doctorRepository.save(doctor);
        return visitMapper.toDto(visitRepository.save(visit));
    }

    @Transactional
    public VisitDto makePatientAnAppointment(Long visitId, Long patientId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("Nie znaleziono terminu wizyty o podanym id"));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Nie znaleziono pacjenta o podanym id"));
        if (visit.getPatient()!=null) {
            throw new VisitAlreadyTakenException("Ten termin wizyty jest już zajęty");
        }
        if (visit.getStartDate().isBefore(LocalDateTime.now())) {
            throw new VisitExpiredException("Ten termin wizyty nie jest już dostępny");
        }
        visit.setPatient(patient);
        patient.addVisit(visit);
        patientRepository.save(patient);
        return visitMapper.toDto(visitRepository.save(visit));
    }

    private void validateVisitDate(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidVisitException("Data początkowa wizyty nie może być po dacie końcowej");
        }
        if (startDate.isEqual(endDate)) {
            throw new InvalidVisitException("Data początkowa wizyty nie może być taka sama jak data końcowa");
        }
        if (startDate.getMinute()%15!=0 || endDate.getMinute()%15!=0 || startDate.getSecond()!=0 || endDate.getSecond()!=0) {
            throw new InvalidVisitException("Godziny wizyt muszą być w pełnym kwadransie godziny");
        }
        if (startDate.isBefore(LocalDateTime.now())) {
            throw new InvalidVisitException("Data wizyty nie może poprzedzać aktualnej daty");
        }
        if (visitRepository.findByStartDateBetween(startDate, endDate).isPresent()
                && visitRepository.findByEndDateBetween(startDate, endDate).isPresent()) {
            throw new InvalidVisitException("Data wizyty pokrywa się z już istniejącą");
        }
    }
}
