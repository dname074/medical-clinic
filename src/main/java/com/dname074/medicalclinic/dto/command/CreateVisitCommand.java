package com.dname074.medicalclinic.dto.command;

import java.time.LocalDateTime;

public record CreateVisitCommand(Long doctorId, LocalDateTime startDate, LocalDateTime endDate) {
}
