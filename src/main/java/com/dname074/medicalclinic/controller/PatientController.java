package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.dto.MedicalClinicExceptionDto;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.command.CreatePatientCommand;
import com.dname074.medicalclinic.dto.command.ChangePasswordCommand;
import com.dname074.medicalclinic.dto.PatientDto;
import com.dname074.medicalclinic.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/patients")
@Tag(name = "Patients operations", description = "Endpoints related to operations on patients")
public class PatientController {
    private final PatientService patientService;

    @Operation(summary = "Get all patients in page based on request params")
    @GetMapping
    public PageDto<PatientDto> findAll(@ParameterObject Pageable pageRequest) {
        log.info("Process of finding all patients with parameters page={} and size={} started", pageRequest.getPageNumber(), pageRequest.getPageSize());
        log.info("Process of finding all patients with parameters page={} and size={} ended", pageRequest.getPageNumber(), pageRequest.getPageSize());
        return patientService.findAll(pageRequest);
    }

    @Operation(summary = "Get patient by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient found",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PatientDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Patient not found",
            content = {@Content(mediaType = "application/json",
            schema = @Schema(implementation = MedicalClinicExceptionDto.class)) })
    })
    @GetMapping("/{patientId}")
    public PatientDto findPatientById(@PathVariable Long patientId) {
        log.info("Process of finding patient by id={}", patientId);
        return patientService.getPatientDtoById(patientId);
    }

    @Operation(summary = "Add new patient to medical clinic system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient created",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PatientDto.class)) }),
            @ApiResponse(responseCode = "409", description = "Patient or User already exists",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            })
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public PatientDto addPatient(@RequestBody @Valid CreatePatientCommand patient) {
        return patientService.addPatient(patient);
    }

    @Operation(summary = "Update existing patient by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient updated",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PatientDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Patient not found",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            })
    })
    @PutMapping("/{patientId}")
    public PatientDto updatePatientById(@PathVariable Long patientId, @RequestBody @Valid CreatePatientCommand updatedPatient) {
        return patientService.updatePatientById(patientId, updatedPatient);
    }

    @Operation(summary = "Delete existing patient from medical clinic by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient deleted",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PatientDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Patient not found",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            })
    })
    @DeleteMapping("/{patientId}")
    public PatientDto deletePatientById(@PathVariable Long patientId) {
        return patientService.deletePatientById(patientId);
    }

    @Operation(summary = "Find patient by id and modify his password")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Patient modified",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PatientDto.class))
                    }),
            @ApiResponse(responseCode = "404", description = "Patient not found",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            })
    })
    @PatchMapping("/{patientId}")
    public PatientDto modifyPasswordById(@PathVariable Long patientId, @RequestBody @Valid ChangePasswordCommand newPassword) {
        return patientService.modifyPatientPasswordById(patientId, newPassword);
    }

    // todo: logi
}
