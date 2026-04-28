package pl.javakurs.dname074.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePatientCommand(
        @Email
        @NotBlank
        String email,
        @Size(min = 8, max = 64)
        @NotBlank
        String password,
        @Size(max = 5)
        @NotBlank
        String idCardNo,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        @Pattern(regexp = "^\\d{9}$")
        String phoneNumber,
        @Past
        LocalDate birthday,
        @Size(min = 36, max = 36)
        @NotBlank
        String keycloakId
        ) {
}
