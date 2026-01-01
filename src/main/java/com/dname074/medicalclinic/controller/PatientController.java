package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.model.CreatePatientCommand;
import com.dname074.medicalclinic.model.ChangePasswordCommand;
import com.dname074.medicalclinic.model.PatientDto;
import com.dname074.medicalclinic.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/patients")
public class PatientController {
    private final PatientService patientService;

    @GetMapping
    public List<PatientDto> findAll() {
        return patientService.findAll();
    }

    @GetMapping("/{email}")
    public ResponseEntity<PatientDto> findPatientByEmail(@PathVariable String email) {
//        Optional<PatientDto> patient = patientService.findPatientByEmail(email);
//        return patient.map(value -> ResponseEntity.ok().body(value)).orElseGet(() -> ResponseEntity.notFound().build()); działa tak samo, ale krótsza i lepsza jest druga opcja
        return ResponseEntity.of(patientService.findPatientByEmail(email));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public PatientDto addPatient(@RequestBody CreatePatientCommand patient) {
        return patientService.addPatient(patient);
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<PatientDto> removePatient(@PathVariable String email) {
        return ResponseEntity.of(patientService.removePatient(email));
    }

    @PutMapping("/{email}")
    public ResponseEntity<PatientDto> updatePatient(@PathVariable String email, @RequestBody CreatePatientCommand updatedPatient) {
        return ResponseEntity.of(patientService.updatePatient(email, updatedPatient));
    }

    @PatchMapping("/{email}")
    public ResponseEntity<PatientDto> modifyPassword(@PathVariable String email, @RequestBody ChangePasswordCommand newPassword) {
        return ResponseEntity.of(patientService.modifyPatientPassword(email, newPassword));
    }
}
