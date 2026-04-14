package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.dto.PatientDto;
import com.dname074.medicalclinic.dto.UserDto;
import com.dname074.medicalclinic.dto.command.ChangePasswordCommand;
import com.dname074.medicalclinic.dto.command.CreatePatientCommand;
import com.dname074.medicalclinic.exception.patient.PatientAlreadyExistsException;
import com.dname074.medicalclinic.exception.patient.PatientNotFoundException;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.service.PatientService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PatientControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    PatientService service;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    PageMapper pageMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAll_PatientFound_PageReturned() throws Exception {
        int page = 0;
        int size = 1;

        Pageable pageable = PageRequest.of(page, size);

        List<PatientDto> patients = List.of(createPatientDto());
        Page<PatientDto> patientsPage = new PageImpl<>(patients, pageable, 1);

        when(service.findAll(pageable))
                .thenReturn(pageMapper.toPatientDto(patientsPage));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service).findAll(pageable);
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findPatientById_Found_Returned() throws Exception {
        Long id = 1L;

        when(service.getPatientDtoById(eq(id), any(Authentication.class)))
                .thenReturn(createPatientDto());

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("email@onet.pl"));

        verify(service).getPatientDtoById(eq(id), any(Authentication.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findPatientById_NotFound_404() throws Exception {
        Long id = 1L;

        when(service.getPatientDtoById(eq(id), any(Authentication.class)))
                .thenThrow(new PatientNotFoundException("Nie udało się znaleźć pacjenta o podanym id"));

        mockMvc.perform(MockMvcRequestBuilders.get("/patients/{id}", id))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Nie udało się znaleźć pacjenta o podanym id"));

        verify(service).getPatientDtoById(eq(id), any(Authentication.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addPatient_Created() throws Exception {
        CreatePatientCommand cmd = makeCreatePatientCommand();

        when(service.addPatient(cmd)).thenReturn(createPatientDto());

        mockMvc.perform(MockMvcRequestBuilders.post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).addPatient(cmd);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addPatient_Invalid_400() throws Exception {
        CreatePatientCommand invalid =
                new CreatePatientCommand(
                        "email",
                        "123",
                        "23",
                        "Jan",
                        "Kowalski",
                        "123456789",
                        LocalDate.of(2000, 1, 2),
                        "000000000000000000000000000000000000"
                );

        mockMvc.perform(MockMvcRequestBuilders.post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addPatient_Conflict() throws Exception {
        CreatePatientCommand cmd = makeCreatePatientCommand();

        when(service.addPatient(cmd))
                .thenThrow(new PatientAlreadyExistsException(
                        "Pacjent o podanym adresie email już istnieje w bazie danych"));

        mockMvc.perform(MockMvcRequestBuilders.post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isConflict());

        verify(service).addPatient(cmd);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePatient_Returned() throws Exception {
        Long id = 1L;
        CreatePatientCommand cmd = makeCreatePatientCommand();

        when(service.updatePatientById(eq(id), eq(cmd), any(Authentication.class)))
                .thenReturn(createPatientDto());

        mockMvc.perform(MockMvcRequestBuilders.put("/patients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).updatePatientById(eq(id), eq(cmd), any(Authentication.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePatient_Returned() throws Exception {
        Long id = 1L;

        when(service.deletePatientById(id)).thenReturn(createPatientDto());

        mockMvc.perform(MockMvcRequestBuilders.delete("/patients/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).deletePatientById(id);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void modifyPassword_Returned() throws Exception {
        Long id = 1L;

        ChangePasswordCommand cmd = new ChangePasswordCommand("password");

        when(service.modifyPatientPasswordById(eq(id), eq(cmd), any(Authentication.class)))
                .thenReturn(createPatientDto());

        mockMvc.perform(MockMvcRequestBuilders.patch("/patients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).modifyPatientPasswordById(eq(id), eq(cmd), any(Authentication.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void modifyPassword_NotFound_404() throws Exception {
        Long id = 1L;

        ChangePasswordCommand cmd = new ChangePasswordCommand("12345678");

        when(service.modifyPatientPasswordById(eq(id), eq(cmd), any(Authentication.class)))
                .thenThrow(new PatientNotFoundException("Nie udało się znaleźć pacjenta o podanym id"));

        mockMvc.perform(MockMvcRequestBuilders.patch("/patients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isNotFound());

        verify(service).modifyPatientPasswordById(eq(id), eq(cmd), any(Authentication.class));
    }

    private CreatePatientCommand makeCreatePatientCommand() {
        return new CreatePatientCommand(
                "email@onet.pl", "password123", "23",
                "Jan", "Kowalski", "123456789",
                LocalDate.of(2000, 1, 2),
                "000000000000000000000000000000000000"
        );
    }

    private PatientDto createPatientDto() {
        return new PatientDto(1L, "email@onet.pl", "23", "123456789",
                LocalDate.of(2000, 1, 2),
                new UserDto(1L, "Jan", "Kowalski"),
                List.of());
    }
}
