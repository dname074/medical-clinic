package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.dto.MedicalClinicExceptionDto;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.VisitDto;
import com.dname074.medicalclinic.dto.command.CreateVisitCommand;
import com.dname074.medicalclinic.service.VisitService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/visits")
@Tag(name = "Visit operations", description = "Endpoints related to operations on visits")
public class VisitController {
    private final VisitService service;

    @Operation(summary = "Get patient's visits by id")
    @GetMapping("/patients")
    public PageDto<VisitDto> getVisitsByPatientId(@RequestParam Long id, @ParameterObject Pageable pageRequest) {
        log.info("Received GET /patients request with parameters: id={}, page={}, size={}", id, pageRequest.getPageNumber(), pageRequest.getPageSize());
        return service.getVisitsByPatientId(id, pageRequest);
    }

    @Operation(summary = "Add available visit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Visit added",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = VisitDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Incorrect visit date",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Doctor not found",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            })
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitDto addVisit(@RequestBody @Valid CreateVisitCommand createVisitCommand) {
        log.info("Received POST /visits request {}", createVisitCommand.toString());
        return service.addAvailableVisit(createVisitCommand);
    }

    @Operation(summary = "Assign patient to visit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient assigned to visit",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = VisitDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Visit expired",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Visit not found or Patient not found",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            }),
            @ApiResponse(responseCode = "409", description = "Visit date already booked",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
            })
    })
    @PatchMapping("/{visitId}/patients/{patientId}")
    public VisitDto assign(@PathVariable Long visitId, @PathVariable Long patientId) {
        log.info("Received PATCH /visits/{}/patients/{}", visitId, patientId);
        return service.assign(visitId, patientId);
    }

    // todo: uzupelnic logi we wszystkich miejscach
}
