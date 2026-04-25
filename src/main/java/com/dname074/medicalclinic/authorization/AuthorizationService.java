package com.dname074.medicalclinic.authorization;

import com.dname074.medicalclinic.model.Role;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationService {
    public Jwt extractJwt(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthenticationCredentialsNotFoundException("Invalid token");
        }
        return jwt;
    }

    public boolean hasRole(Authentication auth, Role role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_" + role));
    }
}
