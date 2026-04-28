package pl.javakurs.dname074.adapter.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class JwtConverterConfig {
    @Value("${app.security.jwt.client-id:medical-clinic-api}")
    private String clientId;

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<GrantedAuthority> authorities = new HashSet<>();
            extractRoles(jwt, "realm_access", authorities);
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null && resourceAccess.containsKey(clientId)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> clientAccess =
                        (Map<String, Object>) resourceAccess.get(clientId);
                if (clientAccess != null && clientAccess.containsKey("roles")) {
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) clientAccess.get("roles");
                    roles.forEach(role ->
                            authorities.add(
                                    new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
                }
            }
            String scope = jwt.getClaimAsString("scope");
            if (scope != null) {
                Arrays.stream(scope.split(" "))
                        .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
                        .forEach(authorities::add);
            }
            return authorities;
        });
        return converter;
    }
    private void extractRoles(Jwt jwt, String claimName,
                              Set<GrantedAuthority> authorities) {
        Map<String, Object> access = jwt.getClaimAsMap(claimName);
        if (access != null && access.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) access.get("roles");
            roles.stream()
                    .filter(role -> !role.startsWith("default-roles-"))
                    .filter(role -> !role.equals("offline_access"))
                    .filter(role -> !role.equals("uma_authorization"))
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .forEach(authorities::add);
        }
    }

}
