package com.dname074.medicalclinic.dto;

import com.dname074.medicalclinic.dto.simple.SimpleDoctorDto;

import java.util.List;

public record InstitutionDto(Long id, String name, String town, String zipCode,
                             String street, Integer placeNo, List<SimpleDoctorDto> doctors) {
}
