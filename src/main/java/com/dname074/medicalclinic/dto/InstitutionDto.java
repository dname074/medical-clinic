package com.dname074.medicalclinic.dto;

public record InstitutionDto(Long id, String name, String town, String zipCode,
                             String street, Integer placeNo) {
}
