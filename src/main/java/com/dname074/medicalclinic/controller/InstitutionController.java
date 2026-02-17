package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.dto.MedicalClinicExceptionDto;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.command.CreateInstitutionCommand;
import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.dto.InstitutionDto;
import com.dname074.medicalclinic.service.InstitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "Institutions operations", description = "Endpoints related to operations on institutions")
public class InstitutionController {
    private final InstitutionService service;

    @Operation(summary = "Get all institutions in page based on request params")
    @GetMapping
    public PageDto<InstitutionDto> findAllInstitutions(Pageable pageRequest) {
        return service.findAllInstitutions(pageRequest);
    }

    @Operation(summary = "Get institution by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Institution found",
        content = {
                @Content(mediaType = "application/json",
                schema = @Schema(implementation = InstitutionDto.class))
        }),
        @ApiResponse(responseCode = "404", description = "Institution not found",
        content = {
                @Content(mediaType = "application/json",
                schema = @Schema(implementation = MedicalClinicExceptionDto.class))
        })
    })
    @GetMapping("/{institutionId}")
    public InstitutionDto findInstitutionById(@PathVariable Long institutionId) {
        return service.getInstitutionDtoById(institutionId);
    }

    @Operation(summary = "Add new institution to medical clinic system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Institution added",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = InstitutionDto.class))
            }),
            @ApiResponse(responseCode = "409", description = "Institution already exists",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            })
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InstitutionDto addInstitution(@RequestBody CreateInstitutionCommand createInstitutionCommand) {
        return service.addInstitution(createInstitutionCommand);
    }

    @Operation(summary = "Update existing institution by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Institution updated",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = InstitutionDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Institution not found",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            })
    })
    @PutMapping("/{institutionId}")
    public InstitutionDto updateInstitutionById(@RequestBody CreateInstitutionCommand createInstitutionCommand, @PathVariable Long institutionId) {
        return service.updateInstitution(createInstitutionCommand, institutionId);
    }

    @Operation(summary = "Assign doctor to institution by id's")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor assigned to institution",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DoctorDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Doctor or institution not found",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            }),
            @ApiResponse(responseCode = "409", description = "Doctor is already assigned to this institution",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            })
    })
    @PatchMapping("/{institutionId}/doctors/{doctorId}")
    public DoctorDto assignDoctorToInstitution(@PathVariable Long institutionId, @PathVariable Long doctorId) {
        return service.assignDoctorToInstitution(doctorId, institutionId);
    }

    @Operation(summary = "Delete institution by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Institution deleted",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = InstitutionDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Institution not found",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            })
    })
    @DeleteMapping("/{institutionId}")
    public InstitutionDto deleteInstitutionById(@PathVariable Long institutionId) {
        return service.deleteInstitutionById(institutionId);
    }

    @Operation(summary = "Remove doctor from institution by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor removed from institution",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DoctorDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Doctor or institution not found",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            })
    })
    @DeleteMapping("/{institutionId}/doctors/{doctorId}")
    public DoctorDto removeDoctorFromInstitution(@PathVariable Long institutionId, @PathVariable Long doctorId) {
        return service.removeDoctorFromInstitution(institutionId, doctorId);
    }
}
