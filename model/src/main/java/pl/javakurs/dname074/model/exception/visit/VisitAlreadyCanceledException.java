package pl.javakurs.dname074.model.exception.visit;

import pl.javakurs.dname074.model.exception.MedicalClinicException;
import org.springframework.http.HttpStatus;

public class VisitAlreadyCanceledException extends MedicalClinicException {
    public VisitAlreadyCanceledException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
