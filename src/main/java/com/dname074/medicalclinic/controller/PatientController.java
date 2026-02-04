package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.dto.command.CreatePatientCommand;
import com.dname074.medicalclinic.dto.command.ChangePasswordCommand;
import com.dname074.medicalclinic.dto.PatientDto;
import com.dname074.medicalclinic.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/patients")
public class PatientController {
    private final PatientService patientService;

    @GetMapping
    public Page<PatientDto> findAll(Pageable pageRequest) {
        return patientService.findAll(pageRequest);
    }

    @GetMapping("/{patientId}")
    public PatientDto findPatientById(@PathVariable Long patientId) {
        return patientService.getPatientDtoById(patientId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public PatientDto addPatient(@RequestBody CreatePatientCommand patient) {
        return patientService.addPatient(patient);
    }

    @DeleteMapping("/{patientId}")
    public PatientDto deletePatientById(@PathVariable Long patientId) {
        return patientService.deletePatientById(patientId);
    }

    @PutMapping("/{patientId}")
    public PatientDto updatePatientById(@PathVariable Long patientId, @RequestBody CreatePatientCommand updatedPatient) {
        return patientService.updatePatientById(patientId, updatedPatient);
    }

    @PatchMapping("/{patientId}")
    public PatientDto modifyPasswordById(@PathVariable Long patientId, @RequestBody ChangePasswordCommand newPassword) {
        return patientService.modifyPatientPasswordById(patientId, newPassword);
    }
}
