package com.dname074.medicalclinic.dto;

public record PageDto<T>(List<T> content, int totalPages, int totalElements,
                         int pageNumber, int pageSize) {
}
