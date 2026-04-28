package pl.javakurs.dname074.domain;

import pl.javakurs.dname074.model.Doctor;

import java.util.Optional;

public interface DoctorRepositoryProvider {
    Doctor save(Doctor doctor);
    void delete(Doctor doctor);
    Page<Doctor> findAll(Specification filters, Pageable pageRequest);
    Optional<Doctor> findById(Long id);
    Optional<Doctor> findByEmail(String email);
    Optional<Doctor> findByFirstNameAndLastName(
            String firstName, String lastName);
}
