package com.dname074.medicalclinic.service;

import com.dname074.medicalclinic.dto.VisitDto;
import com.dname074.medicalclinic.dto.command.CreateVisitCommand;
import com.dname074.medicalclinic.exception.DateAlreadyOccupiedException;
import com.dname074.medicalclinic.exception.DoctorNotFoundException;
import com.dname074.medicalclinic.mapper.VisitMapper;
import com.dname074.medicalclinic.model.Doctor;
import com.dname074.medicalclinic.model.Visit;
import com.dname074.medicalclinic.repository.DoctorRepository;
import com.dname074.medicalclinic.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final VisitMapper visitMapper;

    public VisitDto addAvailableVisit(CreateVisitCommand createVisitCommand) {
        if (!isDateAvailable(createVisitCommand.startDate(), createVisitCommand.endDate())) {
            throw new DateAlreadyOccupiedException("Data wizyty pokrywa się z już istniejącą");
        }
        Doctor doctor = doctorRepository.findById(createVisitCommand.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Nie znaleziono doktora o podanym id"));
        Visit visit = visitMapper.toEntity(createVisitCommand);
        visit.setDoctor(doctor);
        return visitMapper.toDto(visitRepository.save(visit));
    }

    private boolean isDateAvailable(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            return false;
        }
        if (startDate.getMinute()%15!=0 || endDate.getMinute()%15!=0) {
            return false;
        }
        if (startDate.getSecond()!=0 || endDate.getSecond()!=0) {
            return false;
        }
        if (startDate.isBefore(LocalDateTime.now())) {
            return false;
        }
        return visitRepository.findByStartDateBetween(startDate, endDate).isEmpty()
                && visitRepository.findByEndDateBetween(startDate, endDate).isEmpty();
    }
}
