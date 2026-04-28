package pl.javakurs.dname074.model.exception.visit;

import pl.javakurs.dname074.model.exception.MedicalClinicException;
import org.springframework.http.HttpStatus;

public class VisitAlreadyTakenException extends MedicalClinicException {
    public VisitAlreadyTakenException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
