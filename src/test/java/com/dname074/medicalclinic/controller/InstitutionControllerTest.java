package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.dto.DoctorDto;
import com.dname074.medicalclinic.dto.InstitutionDto;
import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.UserDto;
import com.dname074.medicalclinic.dto.command.CreateInstitutionCommand;
import com.dname074.medicalclinic.dto.simple.SimpleInstitutionDto;
import com.dname074.medicalclinic.exception.institution.InstitutionNotFoundException;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.model.Specialization;
import com.dname074.medicalclinic.service.InstitutionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class InstitutionControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    InstitutionService service;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    PageMapper pageMapper;

    @Test
    void findAllInstitutions_PageReturned() throws Exception {
        // given
        int page = 0;
        int size = 1;
        InstitutionDto institutionDto = createInstitution();
        List<InstitutionDto> institutions = List.of(institutionDto);
        Pageable pageable = PageRequest.of(page, size);
        Page<InstitutionDto> institutionsPage = new PageImpl<>(institutions, pageable, 1);
        PageDto<InstitutionDto> institutionsPageDto = pageMapper.toInstitutionDto(institutionsPage);
        when(service.findAllInstitutions(pageable)).thenReturn(institutionsPageDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/institutions")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                )
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(1));
        verify(service, times(1)).findAllInstitutions(pageable);
        verifyNoMoreInteractions(service);
    }

    @Test
    void findInstitutionById_InstitutionFound_InstitutionDtoReturned() throws Exception {
        // given
        Long institutionId = 1L;
        InstitutionDto institutionDto = createInstitution();
        when(service.getInstitutionDtoById(institutionId)).thenReturn(institutionDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/institutions/{institutionId}", institutionId))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Placówka"))
                .andExpect(jsonPath("$.town").value("Katowice"))
                .andExpect(jsonPath("$.zipCode").value("453-2"))
                .andExpect(jsonPath("$.street").value("Szybka"))
                .andExpect(jsonPath("$.placeNo").value(21))
                .andExpect(jsonPath("$.doctors").isEmpty());
        verify(service, times(1)).getInstitutionDtoById(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void findInstitutionById_InstitutionNotFound_404Returned() throws Exception {
        // given
        Long institutionId = 1L;
        when(service.getInstitutionDtoById(institutionId)).thenThrow(new InstitutionNotFoundException("Nie znaleziono instytucji o podanym id"));
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/institutions/{institutionId}", institutionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Nie znaleziono instytucji o podanym id"));
        verify(service,  times(1)).getInstitutionDtoById(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void addInstitution_InstitutionNotFound_InstitutionAddedAndReturned() throws Exception {
        // given
        CreateInstitutionCommand createInstitutionCommand = makeCreateInstitutionCommand();
        InstitutionDto institutionDto = createInstitution();
        when(service.addInstitution(createInstitutionCommand)).thenReturn(institutionDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/institutions")
                .content(objectMapper.writeValueAsString(createInstitutionCommand))
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Placówka"))
                .andExpect(jsonPath("$.town").value("Katowice"))
                .andExpect(jsonPath("$.zipCode").value("453-2"))
                .andExpect(jsonPath("$.street").value("Szybka"))
                .andExpect(jsonPath("$.placeNo").value(21))
                .andExpect(jsonPath("$.doctors").isEmpty());
        verify(service,times(1)).addInstitution(createInstitutionCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void updateInstitutionById_InstitutionFound_InstitutionReturned() throws Exception {
        // given
        Long institutionId = 1L;
        CreateInstitutionCommand createInstitutionCommand = makeCreateInstitutionCommand();
        InstitutionDto institutionDto = createInstitution();
        when(service.updateInstitution(createInstitutionCommand, institutionId)).thenReturn(institutionDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.put("/institutions/{institutionId}", institutionId)
                .content(objectMapper.writeValueAsString(createInstitutionCommand))
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Placówka"))
                .andExpect(jsonPath("$.town").value("Katowice"))
                .andExpect(jsonPath("$.zipCode").value("453-2"))
                .andExpect(jsonPath("$.street").value("Szybka"))
                .andExpect(jsonPath("$.placeNo").value(21))
                .andExpect(jsonPath("$.doctors").isEmpty());
        verify(service, times(1)).updateInstitution(createInstitutionCommand, 1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void assignDoctorToInstitution_DoctorFoundAndInstitutionFound_DoctorDtoReturned() throws Exception {
        // given
        Long institutionId = 1L;
        Long doctorId = 1L;
        SimpleInstitutionDto simpleInstitutionDto = new SimpleInstitutionDto(1L, "Placówka", "Katowice", "453-2",
                "Szybka", 21);
        DoctorDto doctorDto = new DoctorDto(1L, "email", Specialization.DERMATOLOGIST,
                new UserDto(1L, "Jan", "Kowalski"), List.of(simpleInstitutionDto));
        when(service.assignDoctorToInstitution(doctorId, institutionId)).thenReturn(doctorDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.patch("/institutions/{institutionId}/doctors/{doctorId}",
                        institutionId, doctorId))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("email"))
                .andExpect(jsonPath("$.specialization").value("DERMATOLOGIST"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.institutions[0].id").value(1));
        verify(service, times(1)).assignDoctorToInstitution(doctorId, institutionId);
        verifyNoMoreInteractions(service);
    }

    @Test
    void deleteInstitutionById_InstitutionFound_InstitutionDtoReturned() throws Exception {
        // given
        Long institutionId = 1L;
        InstitutionDto institutionDto = createInstitution();
        when(service.deleteInstitutionById(institutionId)).thenReturn(institutionDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.delete("/institutions/{institutionId}", institutionId))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Placówka"))
                .andExpect(jsonPath("$.town").value("Katowice"))
                .andExpect(jsonPath("$.zipCode").value("453-2"))
                .andExpect(jsonPath("$.street").value("Szybka"))
                .andExpect(jsonPath("$.placeNo").value(21))
                .andExpect(jsonPath("$.doctors").isEmpty());
        verify(service, times(1)).deleteInstitutionById(1L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void removeDoctorFromInstitution_RequestCorrect_DoctorDtoReturned() throws Exception {
        // given
        Long institutionId = 1L;
        Long doctorId = 1L;
        DoctorDto doctorDto = new DoctorDto(1L, "email", Specialization.DERMATOLOGIST,
                new UserDto(1L, "Jan", "Kowalski"), List.of());
        when(service.removeDoctorFromInstitution(institutionId, doctorId)).thenReturn(doctorDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.delete("/institutions/{institutionId}/doctors/{doctorId}",
                institutionId, doctorId))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("email"))
                .andExpect(jsonPath("$.specialization").value("DERMATOLOGIST"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.firstName").value("Jan"))
                .andExpect(jsonPath("$.user.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.institutions").isEmpty());
        verify(service, times(1)).removeDoctorFromInstitution(institutionId, doctorId);
        verifyNoMoreInteractions(service);
    }

    private CreateInstitutionCommand makeCreateInstitutionCommand() {
        return new CreateInstitutionCommand("Placówka", "Katowice", "453-2",
                "Szybka", 21);
    }

    private InstitutionDto createInstitution() {
        return new InstitutionDto(1L, "Placówka", "Katowice", "453-2", "Szybka",
                21, List.of());
    }

    // todo: testy walidacji
}
