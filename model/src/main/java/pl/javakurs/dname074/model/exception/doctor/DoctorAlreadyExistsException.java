package pl.javakurs.dname074.model.exception.doctor;

import pl.javakurs.dname074.model.exception.MedicalClinicException;
import org.springframework.http.HttpStatus;

public class DoctorAlreadyExistsException extends MedicalClinicException {
    public DoctorAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
