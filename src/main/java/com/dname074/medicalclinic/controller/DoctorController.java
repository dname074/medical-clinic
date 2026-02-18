package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.dto.MedicalClinicExceptionDto;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.ValidationExceptionDto;
import com.dname074.medicalclinic.dto.command.CreateDoctorCommand;
import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.service.DoctorService;
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
@RequestMapping("/doctors")
@Tag(name = "Doctors operations", description = "Endpoints related to operations on doctors")
public class DoctorController {
    private final DoctorService service;

    @Operation(summary = "Get all doctors in page based on request params")
    @GetMapping
    public PageDto<DoctorDto> findAllDoctors(@ParameterObject Pageable pageRequest) {
        log.info("Received GET /doctors request with parameters: page={}, size={}", pageRequest.getPageNumber(), pageRequest.getPageSize());
        return service.findAllDoctors(pageRequest);
    }

    @Operation(summary = "Get doctor by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor found",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DoctorDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Arguments not valid",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ValidationExceptionDto.class))
                    }),
            @ApiResponse(responseCode = "404", description = "Doctor not found",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            })
    })
    @GetMapping("/{doctorId}")
    public DoctorDto findDoctorById(@PathVariable Long doctorId) {
        log.info("Received GET /doctors/id request with id parameter {}", doctorId);
        return service.getDoctorDtoById(doctorId);
    }

    @Operation(summary = "Add doctor to medical clinic system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Doctor added",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DoctorDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Arguments not valid",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ValidationExceptionDto.class))
                    }),
            @ApiResponse(responseCode = "409", description = "Doctor or user already exists",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            })
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorDto addDoctor(@RequestBody @Valid CreateDoctorCommand createDoctorCommand) {
        log.info("Received POST /doctors request with parameter {}", createDoctorCommand.toString());
        return service.addDoctor(createDoctorCommand);
    }

    @Operation(summary = "Update doctor by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor updated",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DoctorDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Arguments not valid",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ValidationExceptionDto.class))
                    }),
            @ApiResponse(responseCode = "404", description = "Doctor not found",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            })
    })
    @PutMapping("/{doctorId}")
    public DoctorDto updateDoctorById(@PathVariable Long doctorId, @RequestBody @Valid CreateDoctorCommand createDoctorCommand) {
        log.info("Received PUT /doctors/id request with id parameter={} and body={}",doctorId, createDoctorCommand);
        return service.updateDoctorById(doctorId, createDoctorCommand);
    }

    @Operation(summary = "Delete doctor by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor deleted",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DoctorDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Arguments not valid",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ValidationExceptionDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Doctor not found",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            })
    })
    @DeleteMapping("/{doctorId}")
    public DoctorDto deleteDoctorById(@PathVariable Long doctorId) {
        log.info("Received DELETE /doctors/id request with id parameter={}", doctorId);
        return service.deleteDoctorById(doctorId);
    }
}
