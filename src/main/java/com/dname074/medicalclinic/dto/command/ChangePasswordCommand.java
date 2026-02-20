package com.dname074.medicalclinic.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordCommand(
        @NotBlank
        @Size(min = 8, max = 64)
        String password
) {
}
