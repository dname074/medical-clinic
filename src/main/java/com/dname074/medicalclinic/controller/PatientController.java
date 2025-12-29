package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.model.Patient;
import com.dname074.medicalclinic.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/patients")
public class PatientController {
    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<List<Patient>> findAll() {
        return ResponseEntity.ok().body(patientService.findAll());
    }

    @GetMapping("/search")
    @ResponseBody
    public List<Patient> findPatientsByParameters(@RequestParam(value = "firstName", required = false) String firstName,
                                                  @RequestParam(value = "lastName", required = false) String lastName) {
        return patientService.findPatientsByParameters(firstName, lastName);
    }

    @GetMapping("/{email}")
    public ResponseEntity<Patient> findPatientByEmail(@PathVariable String email) {
        return ResponseEntity.ok().body(patientService.findPatientByEmail(email));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Patient addPatient(@RequestBody Patient patient) {
        return patientService.addPatient(patient);
    }

    @DeleteMapping("/delete/{email}")
    public Patient removePatient(@PathVariable String email) {  // mozna tez uzyc innej nazwy -> @PathVariable("email") String patientEmail
        return patientService.removePatient(email);
    }

    @PutMapping("/update/{email}")
    public Patient updatePatient(@PathVariable String email, @RequestBody Patient updatedPatient) {
        return patientService.updatePatient(email, updatedPatient);
    }
    // mozna uzyc kilku path variable w URI
}
