package com.dname074.medicalclinic.argumentmatcher;


import com.dname074.medicalclinic.model.Institution;
import lombok.RequiredArgsConstructor;
import org.mockito.ArgumentMatcher;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class InstitutionArgumentMatcher implements ArgumentMatcher<Institution> {
    private final Institution institution;

    @Override
    public boolean matches(Institution newInstitution) {
        return nonNull(newInstitution) &&
                newInstitution.getName().equalsIgnoreCase(this.institution.getName()) &&
                newInstitution.getTown().equalsIgnoreCase(this.institution.getTown()) &&
                newInstitution.getZipCode().equalsIgnoreCase(this.institution.getZipCode()) &&
                newInstitution.getStreet().equalsIgnoreCase(this.institution.getStreet()) &&
                newInstitution.getPlaceNo().equals(this.institution.getPlaceNo());
    }
}
