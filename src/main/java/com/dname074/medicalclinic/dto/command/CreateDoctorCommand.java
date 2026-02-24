package com.dname074.medicalclinic.dto.command;

import com.dname074.medicalclinic.model.Specialization;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDoctorCommand(
        @Email
        @NotBlank
        String email,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        @NotBlank
        @Size(min = 8, max = 64)
        String password,
        Specialization specialization) {
}
