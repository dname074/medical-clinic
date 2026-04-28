package pl.javakurs.dname074.adapter;

import com.dname074.medicalclinic.model.Visit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long>, JpaSpecificationExecutor<Visit> {
    boolean existsByStartDateLessThanAndEndDateGreaterThan(LocalDateTime endDate, LocalDateTime startDate);

    @Override
    @EntityGraph(attributePaths = {
            "doctor",
            "doctor.user",
            "patient",
            "patient.user"
    })
    Page<Visit> findAll(@Nullable Specification<Visit> specification, Pageable pageable);
}
