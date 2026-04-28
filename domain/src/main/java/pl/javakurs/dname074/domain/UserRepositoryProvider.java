package pl.javakurs.dname074.domain;

import pl.javakurs.dname074.model.User;

import java.util.Optional;

public interface UserRepositoryProvider {
    Optional<User> findByFirstNameAndLastName(
            String firstName, String lastName);
}
