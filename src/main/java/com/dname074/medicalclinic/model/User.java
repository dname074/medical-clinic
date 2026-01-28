package com.dname074.medicalclinic.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    // Problem polegał na tym, że przy wysyłaniu requesta aby pobrać placówki, albo doktorów to hibernate wysyła również niepotrzebnie zapytanie o patients
    // teraz user nie wie o patient i doctor, natomiast doctor i patient wiedzą o userze, w ten sposob nie bedzie niepotrzebnych zapytan do bazy
//    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
//    private Patient patient;
//    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
//    private Doctor doctor;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
