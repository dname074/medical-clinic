package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.dto.CreateDoctorCommand;
import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/doctors")
public class DoctorController {
    private final DoctorService service;

    @GetMapping
    public List<DoctorDto> findAllDoctors() {
        return service.findAllDoctors();
    }

    @GetMapping("/{doctorId}")
    public DoctorDto findDoctorById(@PathVariable Long doctorId) {
        return service.getDoctorDtoById(doctorId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorDto addDoctor(@RequestBody CreateDoctorCommand createDoctorCommand) {
        return service.addDoctor(createDoctorCommand);
    }

    @PutMapping("/{doctorId}")
    public DoctorDto updateDoctorById(@PathVariable Long doctorId, @RequestBody CreateDoctorCommand createDoctorCommand) {
        return service.updateDoctorById(doctorId, createDoctorCommand);
    }

    @DeleteMapping("/{doctorId}")
    public DoctorDto deleteDoctorById(@PathVariable Long doctorId) {
        return service.deleteDoctorById(doctorId);
    }
}
