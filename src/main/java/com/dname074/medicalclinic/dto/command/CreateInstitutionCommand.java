package com.dname074.medicalclinic.dto.command;

public record CreateInstitutionCommand(String name, String town, String zipCode,
                                       String street, Integer placeNo) {
}
