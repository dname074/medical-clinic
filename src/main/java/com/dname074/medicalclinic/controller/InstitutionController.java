package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.dto.command.CreateInstitutionCommand;
import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.dto.InstitutionDto;
import com.dname074.medicalclinic.service.InstitutionService;
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
@RequestMapping("/institutions")
public class InstitutionController {
    private final InstitutionService service;

    @GetMapping
    public Page<InstitutionDto> findAllInstitutions(Pageable pageRequest) {
        return service.findAllInstitutions(pageRequest);
    }

    @GetMapping("/{institutionId}")
    public InstitutionDto findInstitutionById(@PathVariable Long id) {
        return service.getInstitutionDtoById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InstitutionDto addInstitutionById(@RequestBody CreateInstitutionCommand createInstitutionCommand) {
        return service.addInstitution(createInstitutionCommand);
    }

    @PutMapping("/{institutionId}")
    public InstitutionDto updateInstitutionById(@RequestBody CreateInstitutionCommand createInstitutionCommand, @PathVariable Long institutionId) {
        return service.updateInstitution(createInstitutionCommand, institutionId);
    }

    @PatchMapping("/{institutionId}/doctors/{doctorId}")
    public DoctorDto assignDoctorToInstitution(@PathVariable Long institutionId, @PathVariable Long doctorId) {
        return service.assignDoctorToInstitution(doctorId, institutionId);
    }

    @DeleteMapping("/{institutionId}")
    public InstitutionDto deleteInstitutionById(@PathVariable Long institutionId) {
        return service.deleteInstitutionById(institutionId);
    }

    @DeleteMapping("/{institutionId}/doctors/{doctorId}")
    public DoctorDto removeDoctorFromInstitution(@PathVariable Long institutionId, @PathVariable Long doctorId) {
        return service.removeDoctorFromInstitution(institutionId, doctorId);
    }
}
