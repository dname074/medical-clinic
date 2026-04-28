package pl.javakurs.dname074.model.exception.patient;

import pl.javakurs.dname074.model.exception.MedicalClinicException;
import org.springframework.http.HttpStatus;

public class PatientAlreadyExistsException extends MedicalClinicException {
    public PatientAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
