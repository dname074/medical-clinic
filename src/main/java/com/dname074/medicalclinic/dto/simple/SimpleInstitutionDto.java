package com.dname074.medicalclinic.dto.simple;

public record SimpleInstitutionDto(Long id, String name, String town, String zipCode,
                                   String street, Integer placeNo) {
}
