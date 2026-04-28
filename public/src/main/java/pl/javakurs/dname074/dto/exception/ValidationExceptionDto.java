package pl.javakurs.dname074.dto.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public record ValidationExceptionDto(HttpStatus status, List<String> messages) {
}
