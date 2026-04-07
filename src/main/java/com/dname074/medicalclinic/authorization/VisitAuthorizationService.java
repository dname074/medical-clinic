package com.dname074.medicalclinic.authorization;

import com.dname074.medicalclinic.model.VisitStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class VisitAuthorizationService {
    public boolean canAccessDoctorVisits(VisitStatus status, Authentication auth) {
        boolean isDoctorOrAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR")
                                                        || a.getAuthority().equals("ROLE_ADMIN"));
        boolean isPatient = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));

        if (status == VisitStatus.AVAILABLE) {
            return isPatient || isDoctorOrAdmin;
        }

        return isDoctorOrAdmin;
    }
}
