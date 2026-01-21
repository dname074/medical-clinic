package com.dname074.medicalclinic.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Specialization {
    PSYCHIATRIST,
    SURGEON,
    DERMATOLOGIST,
    CARDIOLOGIST
}
