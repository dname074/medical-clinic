package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.dto.VisitDto;
import com.dname074.medicalclinic.dto.command.CreateVisitCommand;
import com.dname074.medicalclinic.service.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/visits")
public class VisitController {
    private final VisitService service;

    @GetMapping("/patients")
    public Page<VisitDto> getVisitsByPatientId(@RequestParam Long id, Pageable pageRequest) {
        return service.getVisitsByPatientId(id, pageRequest);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitDto addVisit(@RequestBody CreateVisitCommand createVisitCommand) {
        return service.addAvailableVisit(createVisitCommand);
    }

    @PatchMapping("/{visitId}/patients/{patientId}")
    public VisitDto assign(@PathVariable Long visitId, @PathVariable Long patientId) {
        return service.assign(visitId, patientId);
    }
}
