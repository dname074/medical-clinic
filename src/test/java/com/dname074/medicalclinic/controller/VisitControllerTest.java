package com.dname074.medicalclinic.controller;

import com.dname074.medicalclinic.dto.PageDto;
import com.dname074.medicalclinic.dto.UserDto;
import com.dname074.medicalclinic.dto.VisitDto;
import com.dname074.medicalclinic.dto.command.CreateVisitCommand;
import com.dname074.medicalclinic.dto.simple.SimpleDoctorDto;
import com.dname074.medicalclinic.dto.simple.SimplePatientDto;
import com.dname074.medicalclinic.exception.visit.InvalidVisitException;
import com.dname074.medicalclinic.mapper.PageMapper;
import com.dname074.medicalclinic.model.Specialization;
import com.dname074.medicalclinic.service.VisitService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
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
    void getVisitsByPatientId_PatientFound_VisitsPageReturned() throws Exception {
        // given
        Long patientId = 1L;
        int page = 0;
        int size = 1;

        Pageable pageable = PageRequest.of(page, size);
        VisitDto visitDto = createVisit();
        List<VisitDto> visits = List.of(visitDto);
        Page<VisitDto> visitsPage = new PageImpl<>(visits, pageable, 1);
        PageDto<VisitDto> visitsPageDto = pageMapper.toVisitDto(visitsPage);
        when(service.getVisitsByPatientId(patientId, pageable)).thenReturn(visitsPageDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/visits/patients")
                        .param("id", String.valueOf(patientId))
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
        )
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(1));
        verify(service, times(1)).getVisitsByPatientId(1L, pageable);
        verifyNoMoreInteractions(service);
    }

    @Test
    void addVisit_VisitNotFound_VisitDtoReturned() throws Exception {
        // given
        CreateVisitCommand createVisitCommand = makeCreateVisitCommand();
        VisitDto visitDto = new VisitDto(1L, LocalDateTime.of(2027, 1, 1, 20, 0, 0),
                LocalDateTime.of(2027, 1, 1, 21, 0, 0),
                new SimpleDoctorDto(1L, "email", Specialization.DERMATOLOGIST, new UserDto(1L, "Jan", "Kowalski")),
                null);
        when(service.addAvailableVisit(createVisitCommand)).thenReturn(visitDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/visits")
                .content(objectMapper.writeValueAsString(createVisitCommand))
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.startDate").value("2027-01-01T20:00:00"))
                .andExpect(jsonPath("$.endDate").value("2027-01-01T21:00:00"))
                .andExpect(jsonPath("$.doctor.id").value(1))
                .andExpect(jsonPath("$.patient").isEmpty());
        verify(service, times(1)).addAvailableVisit(createVisitCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void addVisit_VisitFound_409Returned() throws Exception {
        CreateVisitCommand createVisitCommand = makeCreateVisitCommand();
        when(service.addAvailableVisit(createVisitCommand)).thenThrow(new InvalidVisitException("Data wizyty pokrywa się z już istniejącą"));

        mockMvc.perform(MockMvcRequestBuilders.post("/visits")
                .content(objectMapper.writeValueAsString(createVisitCommand))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Data wizyty pokrywa się z już istniejącą"));
        verify(service, times(1)).addAvailableVisit(createVisitCommand);
        verifyNoMoreInteractions(service);
    }

    @Test
    void assign_VisitFoundAndPatientFound_VisitDtoReturned() throws Exception {
        // given
        Long visitId = 1L;
        Long patientId = 1L;
        VisitDto visitDto = createVisit();
        when(service.assign(visitId, patientId)).thenReturn(visitDto);
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.patch("/visits/{visitId}/patients/{patientId}", visitId, patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.startDate").value("2027-01-01T20:00:00"))
                .andExpect(jsonPath("$.endDate").value("2027-01-01T21:00:00"))
                .andExpect(jsonPath("$.doctor.id").value(1))
                .andExpect(jsonPath("$.patient.id").value(1));
        verify(service, times(1)).assign(1L, 1L);
        verifyNoMoreInteractions(service);
    }

    private CreateVisitCommand makeCreateVisitCommand() {
        return new CreateVisitCommand(1L,
                LocalDateTime.of(2027, 1, 1, 20, 0, 0),
                LocalDateTime.of(2027, 1, 1, 21, 0, 0));
    }

    private VisitDto createVisit() {
        return new VisitDto(1L, LocalDateTime.of(2027, 1, 1, 20, 0, 0),
                LocalDateTime.of(2027, 1, 1, 21, 0, 0),
                new SimpleDoctorDto(1L, "email", Specialization.DERMATOLOGIST, new UserDto(1L, "Jan", "Kowalski")),
                new SimplePatientDto(1L, "email2", "23","123456789",
                        LocalDate.of(2001, 1, 1), new UserDto(2L, "Karol", "Nowak")));
    }
}
