package com.dname074.medicalclinic.controller;

import pl.javakurs.dname074.dto.command.CreateVisitCommand;
import pl.javakurs.dname074.dto.simple.SimpleDoctorDto;
import pl.javakurs.dname074.dto.simple.SimplePatientDto;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.model.Specialization;
import com.dname074.medicalclinic.model.VisitStatus;
import com.dname074.medicalclinic.service.VisitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class VisitControllerTest {

    @MockitoBean
    VisitService service;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PageMapper pageMapper;

    @Test
    @WithMockUser(roles = {"PATIENT"})
    void getVisitsByPatientId_PatientFound_VisitsPageReturned() throws Exception {
        Long patientId = 1L;
        int page = 0;
        int size = 1;

        Pageable pageable = PageRequest.of(page, size);

        Page<VisitDto> visitsPage = new PageImpl<>(List.of(createVisit()), pageable, 1);
        PageDto<VisitDto> visitsPageDto = pageMapper.toVisitDto(visitsPage);

        when(service.getVisitsByPatientId(eq(patientId), eq(pageable)))
                .thenReturn(visitsPageDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/visits/patients/{patientId}", patientId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(1));

        verify(service).getVisitsByPatientId(eq(patientId), eq(pageable));
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = {"PATIENT"})
    void getVisitsByDoctorId_DoctorFound_VisitsPageReturned() throws Exception {
        Long doctorId = 1L;
        VisitStatus status = VisitStatus.AVAILABLE;
        int page = 0;
        int size = 1;

        Pageable pageable = PageRequest.of(page, size);

        Page<VisitDto> visitsPage = new PageImpl<>(List.of(createFreeVisit()), pageable, 1);
        PageDto<VisitDto> visitsPageDto = pageMapper.toVisitDto(visitsPage);

        when(service.getVisitsByDoctorId(eq(doctorId), eq(status), eq(pageable), any(Authentication.class)))
                .thenReturn(visitsPageDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/visits/doctors")
                        .param("id", String.valueOf(doctorId))
                        .param("status", status.toString())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(1));

        verify(service).getVisitsByDoctorId(eq(doctorId), eq(status), eq(pageable), any(Authentication.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = {"PATIENT"})
    void getFilteredVisits_FreeVisitFound_VisitsPageReturned() throws Exception {
        Specialization specialization = Specialization.DERMATOLOGIST;
        LocalDate fromDate = LocalDate.of(2027, 1, 1);
        LocalDate toDate = LocalDate.of(2027, 5, 1);
        VisitStatus status = VisitStatus.AVAILABLE;

        int page = 0;
        int size = 1;

        Pageable pageable = PageRequest.of(page, size);

        Page<VisitDto> visitsPage = new PageImpl<>(List.of(createFreeVisit()), pageable, 1);
        PageDto<VisitDto> visitsPageDto = pageMapper.toVisitDto(visitsPage);

        when(service.getFilteredVisits(eq(fromDate), eq(toDate), eq(specialization), eq(status), eq(pageable), any(Authentication.class)))
                .thenReturn(visitsPageDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/visits")
                        .param("from", fromDate.toString())
                        .param("to", toDate.toString())
                        .param("specialization", specialization.toString())
                        .param("status", status.toString())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service).getFilteredVisits(eq(fromDate), eq(toDate), eq(specialization), eq(status), eq(pageable), any(Authentication.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(roles = {"DOCTOR"})
    void addVisit_VisitCreated_Returned() throws Exception {
        CreateVisitCommand cmd = makeCreateVisitCommand();

        VisitDto visitDto = createVisit();

        when(service.addAvailableVisit(eq(cmd), any(Authentication.class)))
                .thenReturn(visitDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/visits")
                        .content(objectMapper.writeValueAsString(cmd))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patient").isNotEmpty());

        verify(service).addAvailableVisit(eq(cmd), any(Authentication.class));
    }

    @Test
    @WithMockUser(roles = {"DOCTOR"})
    void addVisit_InvalidVisitException_400Returned() throws Exception {
        CreateVisitCommand cmd = makeCreateVisitCommand();

        when(service.addAvailableVisit(eq(cmd), any(Authentication.class)))
                .thenThrow(new InvalidVisitException("Data wizyty pokrywa się z już istniejącą"));

        mockMvc.perform(MockMvcRequestBuilders.post("/visits")
                        .content(objectMapper.writeValueAsString(cmd))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Data wizyty pokrywa się z już istniejącą"));

        verify(service).addAvailableVisit(eq(cmd), any(Authentication.class));
    }

    @Test
    @WithMockUser(roles = {"DOCTOR"})
    void addVisit_InvalidBody_400Returned() throws Exception {
        CreateVisitCommand invalid = new CreateVisitCommand(null,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1));

        mockMvc.perform(MockMvcRequestBuilders.post("/visits")
                        .content(objectMapper.writeValueAsString(invalid))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = {"PATIENT"})
    void assign_Success_Returned() throws Exception {
        Long visitId = 1L;
        Long patientId = 1L;

        when(service.assign(eq(visitId), eq(patientId), any(Authentication.class)))
                .thenReturn(createVisit());

        mockMvc.perform(MockMvcRequestBuilders.patch("/visits/{visitId}/patients/{patientId}", visitId, patientId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).assign(eq(visitId), eq(patientId), any(Authentication.class));
    }

    @Test
    @WithMockUser(roles = {"PATIENT"})
    void assign_NotFound_404Returned() throws Exception {
        Long visitId = 1L;
        Long patientId = 1L;

        when(service.assign(eq(visitId), eq(patientId), any(Authentication.class)))
                .thenThrow(new VisitNotFoundException("Nie znaleziono"));

        mockMvc.perform(MockMvcRequestBuilders.patch("/visits/{visitId}/patients/{patientId}", visitId, patientId))
                .andExpect(status().isNotFound());

        verify(service).assign(eq(visitId), eq(patientId), any(Authentication.class));
    }

    private CreateVisitCommand makeCreateVisitCommand() {
        return new CreateVisitCommand(1L,
                LocalDateTime.of(2027, 1, 1, 20, 0),
                LocalDateTime.of(2027, 1, 1, 21, 0));
    }

    private VisitDto createVisit() {
        return new VisitDto(1L,
                LocalDateTime.of(2027, 1, 1, 20, 0),
                LocalDateTime.of(2027, 1, 1, 21, 0),
                VisitStatus.AVAILABLE,
                new SimpleDoctorDto(1L, "email", Specialization.DERMATOLOGIST,
                        new UserDto(1L, "Jan", "Kowalski")),
                new SimplePatientDto(1L, "email2", "23", "123456789",
                        LocalDate.of(2001, 1, 1),
                        new UserDto(2L, "Karol", "Nowak")));
    }

    private VisitDto createFreeVisit() {
        return new VisitDto(1L,
                LocalDateTime.of(2027, 1, 1, 20, 0),
                LocalDateTime.of(2027, 1, 1, 21, 0),
                VisitStatus.AVAILABLE,
                new SimpleDoctorDto(1L, "email", Specialization.DERMATOLOGIST,
                        new UserDto(1L, "Jan", "Kowalski")),
                null);
    }
}