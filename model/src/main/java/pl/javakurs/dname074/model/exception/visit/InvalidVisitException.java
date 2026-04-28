package pl.javakurs.dname074.model.exception.visit;

import pl.javakurs.dname074.model.exception.MedicalClinicException;
import org.springframework.http.HttpStatus;

public class InvalidVisitException extends MedicalClinicException {
    public InvalidVisitException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
