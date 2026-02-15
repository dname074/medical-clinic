package com.dname074.medicalclinic.dto;

import java.util.List;

public record PageDto<T>(List<T> content, int totalPages, int totalElements,
                         int pageNumber, int pageSize) {
}
