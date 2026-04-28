package pl.javakurs.dname074.model.exception.institution;

import pl.javakurs.dname074.model.exception.MedicalClinicException;
import org.springframework.http.HttpStatus;

public class InstitutionExistsException extends MedicalClinicException {
    public InstitutionExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
