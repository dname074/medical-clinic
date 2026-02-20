package com.dname074.medicalclinic.dto.command;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateVisitCommand(
        @NotNull
        Long doctorId,
        @Future
        LocalDateTime startDate,
        @Future
        LocalDateTime endDate) {
}
