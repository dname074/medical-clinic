package pl.javakurs.dname074.adapter;

import pl.javakurs.dname074.dto.exception.MedicalClinicExceptionDto;
import com.dname074.medicalclinic.dto.PageDto;
import pl.javakurs.dname074.dto.exception.ValidationExceptionDto;
import pl.javakurs.dname074.dto.CreatePatientCommand;
import pl.javakurs.dname074.dto.ChangePasswordCommand;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public PageDto<PatientDto> findAll(@ParameterObject Pageable pageRequest) {
        log.info("Received GET /patients request with parameters page={} and size={}", pageRequest.getPageNumber(), pageRequest.getPageSize());
        return patientService.findAll(pageRequest);
    }

    @Operation(summary = "Get patient by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient found",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PatientDto.class))
                    }),
            @ApiResponse(responseCode = "404", description = "Patient not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MedicalClinicExceptionDto.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Not valid arguments passed",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ValidationExceptionDto.class))
                    })
    })
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    @GetMapping("/{patientId}")
    public PatientDto findPatientById(@PathVariable Long patientId,
                                      Authentication auth) {
        log.info("Received GET /patients/id request with id parameter={}", patientId);
        return patientService.getPatientDtoById(patientId, auth);
    }

    @Operation(summary = "Add new patient to medical clinic system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Patient created",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PatientDto.class))}),
            @ApiResponse(responseCode = "409", description = "Patient or User already exists",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Not valid arguments passed",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ValidationExceptionDto.class))
                    })
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public PatientDto addPatient(@RequestBody @Valid CreatePatientCommand patient) {
        log.info("Received POST /patients request with body={}", patient.toString());
        return patientService.addPatient(patient);
    }

    @Operation(summary = "Update existing patient by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patient updated",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PatientDto.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Not valid arguments passed",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ValidationExceptionDto.class))
                    }),
            @ApiResponse(responseCode = "404", description = "Patient not found",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
                    })
    })
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    @PutMapping("/{patientId}")
    public PatientDto updatePatientById(@PathVariable Long patientId,
                                        @RequestBody @Valid CreatePatientCommand updatedPatient,
                                        Authentication auth) {
        log.info("Received PUT /patients/id with id parameter={} and body={}", patientId, updatedPatient);
        return patientService.updatePatientById(patientId, updatedPatient, auth);
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
                    }),
            @ApiResponse(responseCode = "404", description = "Patient not found",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MedicalClinicExceptionDto.class))
                    })
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{patientId}")
    public PatientDto deletePatientById(@PathVariable Long patientId) {
        log.info("Received DELETE /patients/id request with id parameter={}", patientId);
        return patientService.deletePatientById(patientId);
    }

    @Operation(summary = "Find patient by id and modify his password")
    @ApiResponses(value = {
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
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    @PatchMapping("/{patientId}")
    public PatientDto modifyPasswordById(@PathVariable Long patientId,
                                         @RequestBody @Valid ChangePasswordCommand newPassword,
                                         Authentication auth) {
        log.info("Received PATCH /patients/id request with id parameter={} and body={}", patientId, newPassword);
        return patientService.modifyPatientPasswordById(patientId, newPassword, auth);
    }
}
