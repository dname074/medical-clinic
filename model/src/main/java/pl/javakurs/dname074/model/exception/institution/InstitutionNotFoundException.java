package pl.javakurs.dname074.model.exception.institution;

import pl.javakurs.dname074.model.exception.MedicalClinicException;
import org.springframework.http.HttpStatus;

public class InstitutionNotFoundException extends MedicalClinicException {
    public InstitutionNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
