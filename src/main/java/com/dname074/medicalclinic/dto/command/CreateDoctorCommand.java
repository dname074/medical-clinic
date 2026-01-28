package com.dname074.medicalclinic.dto.command;

import com.dname074.medicalclinic.model.Specialization;

public record CreateDoctorCommand(String email, String firstName, String lastName,
                                  String password, Specialization specialization) {
}
