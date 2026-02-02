package com.dname074.medicalclinic.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "visits")
public class Visit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private Patient patient;
    @ManyToOne
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private Doctor doctor;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Visit visit = (Visit) o;
        return id != null && Objects.equals(id, visit.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

//    @Override
//    public String toString() {
//        return "Visit{" +
//                "id=" + id +
//                ", startDateTime=" + startDateTime +
//                ", endDateTime=" + endDateTime +
//                ", patient=" + patient +
//                ", doctor=" + doctor +
//                '}';
//    }
}
