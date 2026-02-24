package com.dname074.medicalclinic.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateInstitutionCommand(
        @NotBlank
        String name,
        @NotBlank
        String town,
        @Pattern(regexp = "^\\d{2}-\\d{3}$")
        String zipCode,
        @NotBlank
        String street,
        @NotNull
        Integer placeNo) {
}
